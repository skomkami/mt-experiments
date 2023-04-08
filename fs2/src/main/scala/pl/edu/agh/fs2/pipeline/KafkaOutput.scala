package pl.edu.agh.fs2.pipeline
import cats.effect.IO
import fs2.kafka.*
import io.circe.Encoder
import pl.edu.agh.model.JsonCodec
import record.ProcessingRecord

import java.util.UUID

case class KafkaOutput[T: Encoder](topic: String) extends OutputWithOffsetCommit[T] {

  private val messageSerializer = Serializer.lift[IO, T] { msg =>
    IO.pure(JsonCodec.toJson(msg).getBytes("UTF-8"))
  }

  private val producerSettings = ProducerSettings(
    keySerializer = Serializer[IO, String],
    valueSerializer = messageSerializer
  ).withBootstrapServers("localhost:9092")

  override def elementSink: fs2.Pipe[IO, ProcessingRecord[T], ProducerResult[String, T]] =
    (s1: fs2.Stream[IO, ProcessingRecord[T]]) =>
      s1.map { msg =>
          val partition = msg.meta.map(_.partition).getOrElse(0)
          val record =
            ProducerRecord(topic, UUID.randomUUID().toString, msg.value)
              .withPartition(partition)
          val records: ProducerRecords[String, T] =
            ProducerRecords.one(record)
          records
        }
        .through(KafkaProducer.pipe(producerSettings))
}
