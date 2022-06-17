package pl.edu.agh.fs2.pipeline

import cats.data.NonEmptySet
import cats.effect.IO
import fs2.kafka._
import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable
import record.ProcessingRecord

import scala.concurrent.duration._

case class KafkaInput[T: DerivedDecoder](topic: String, consumerName: String)(
  implicit decoder: JsonDeserializable[T]
) extends Input[T] {

  val messageDeserializer = Deserializer.instance { (_, _, bytes) =>
    val either = decoder.fromJson(new String(bytes))
    IO.fromEither(either)
  }
  val consumerSettings =
    ConsumerSettings[IO, String, T](
      keyDeserializer = Deserializer[IO, String],
      valueDeserializer = messageDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId(consumerName)
      .withCloseTimeout(1.minute)

  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): fs2.Stream[IO, ProcessingRecord[T]] = {
    val partitionsSortedSet = collection.immutable.SortedSet.from(partitions)
    KafkaConsumer
      .stream(consumerSettings)
      .evalTap(_.assign(topic, NonEmptySet.fromSetUnsafe(partitionsSortedSet)))
      .partitionedRecords
      .parJoinUnbounded
      .map { cr =>
        val meta = KafkaRecordMeta(cr.record.partition, cr.offset)
        record.ProcessingRecord(cr.record.value, Some(meta))
      }
  }
}
