package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.kafka._
import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.common.TopicPartition
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
    val tps = partitions.map(p => new TopicPartition(topic, p))
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic)
      .flatMap(_.partitionsMapStream)
      .map(_.toSeq)
      .flatMap(fs2.Stream.emits)
      .collect { case (tp, stream) if tps.contains(tp) => stream }
      .flatMap(identity)
      .map { cr =>
        val meta = KafkaRecordMeta(cr.record.partition, cr.offset)
        record.ProcessingRecord(cr.record.value, Some(meta))
      }
  }
}
