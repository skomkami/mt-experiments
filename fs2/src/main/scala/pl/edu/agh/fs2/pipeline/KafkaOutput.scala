package pl.edu.agh.fs2.pipeline
import cats.effect.IO
import fs2.kafka._
import io.circe.generic.encoding.DerivedAsObjectEncoder
import pl.edu.agh.model.JsonSerializable

import java.util.UUID

case class KafkaOutput[T: DerivedAsObjectEncoder](topic: String)(
  implicit encoder: JsonSerializable[T]
) extends Output[T] {

  val messageSerializer = Serializer.lift[IO, T] { msg =>
    IO.pure(encoder.toJson(msg).getBytes("UTF-8"))
  }

  val producerSettings = ProducerSettings(
    keySerializer = Serializer[IO, String],
    valueSerializer = messageSerializer
  ).withBootstrapServers("localhost:9092")

  type Producer = ProducerRecords[Unit, String, T]
  type ProducerRes = ProducerResult[Unit, String, T]

  override def sink: fs2.Pipe[IO, T, _] =
    (s1: fs2.Stream[IO, T]) =>
      s1.map { msg =>
          val record = ProducerRecord(topic, UUID.randomUUID().toString, msg)
          val records: ProducerRecords[Unit, String, T] =
            ProducerRecords.one(record)

          records
        }
        .through(KafkaProducer.pipe(producerSettings))
}
