package pl.edu.agh.fs2.pipeline
import cats.effect.IO
import fs2.kafka._
import io.circe.generic.encoding.DerivedAsObjectEncoder
import pl.edu.agh.model.JsonSerializable
import record.ProcessingRecord

import java.util.UUID

case class KafkaOutput[T: DerivedAsObjectEncoder](
  topic: String,
  override val shutdownWhen: T => Boolean = (_: T) => false
)(implicit encoder: JsonSerializable[T])
    extends OutputWithOffsetCommit[T] {

  val messageSerializer = Serializer.lift[IO, T] { msg =>
    IO.pure(encoder.toJson(msg).getBytes("UTF-8"))
  }

  val producerSettings = ProducerSettings(
    keySerializer = Serializer[IO, String],
    valueSerializer = messageSerializer
  ).withBootstrapServers("localhost:9092")

  override def elementSink: fs2.Pipe[IO, ProcessingRecord[T], _] =
    (s1: fs2.Stream[IO, ProcessingRecord[T]]) =>
      s1.map { msg =>
          val partition = msg.meta.map(_.partition).getOrElse(0)
          val record =
            ProducerRecord(topic, UUID.randomUUID().toString, msg.value)
              .withPartition(partition)
          val records: ProducerRecords[Unit, String, T] =
            ProducerRecords.one(record)
          records
        }
        .through(KafkaProducer.pipe(producerSettings))
}
