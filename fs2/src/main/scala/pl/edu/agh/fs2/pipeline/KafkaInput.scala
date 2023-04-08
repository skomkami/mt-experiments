package pl.edu.agh.fs2.pipeline

import io.circe.Decoder
import pl.edu.agh.model.JsonCodec
import cats.data.NonEmptySet
import cats.effect.IO
import fs2.kafka._
import record.ProcessingRecord
import scala.concurrent.duration.*

case class KafkaInput[T: Decoder](topic: String, consumerName: String) extends Input[T] {

  private val messageDeserializer = Deserializer.instance { (_, _, bytes) =>
    val either = JsonCodec.fromJsonSafe(new String(bytes))
    IO.fromEither(either)
  }
  private val consumerSettings =
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
