package pl.edu.agh

import cats.effect._
import cats.implicits.toTraverseOps
import fs2.{Pipe, Pure, Stream}
import fs2.kafka._
import io.circe.syntax._
import pl.edu.agh.FS2ConsumerExample.consumerSettings
import pl.edu.agh.generator.Generator
import pl.edu.agh.msg.RandomMessage

import scala.concurrent.duration.DurationInt

object FS2ProducerExample extends IOApp {
  val messageSerializer = Serializer.lift[IO, RandomMessage] { rm =>
    IO.pure(RandomMessage.toJson(rm).getBytes("UTF-8"))
  }

  val producerSettings = ProducerSettings(
    keySerializer = Serializer[IO, String],
    valueSerializer = messageSerializer
  ).withBootstrapServers("localhost:9092")

  type Producer = ProducerRecords[Unit, String, RandomMessage]
  type ProducerRes = ProducerResult[Unit, String, RandomMessage]

  def run(args: List[String]): IO[ExitCode] = {
//    val stream =
//      KafkaProducer.stream(producerSettings).flatMap{ producer =>
//        val rm = Generator.generateMsg()
//        val record = ProducerRecord("messages", rm.id, rm)
//        val records: ProducerRecords[Unit, String, RandomMessage] = ProducerRecords.one(record)
//        val producerStream: Pipe[IO, Producer, ProducerRes] = KafkaProducer.pipe(producerSettings, producer)
//        Stream(records)
//          .through(producerStream)
//      }

    val rm = Generator.generateMsg()
    val record = ProducerRecord("messages", rm.id, rm)
    val records: ProducerRecords[Unit, String, RandomMessage] =
      ProducerRecords.one(record)
    val producerStream: Pipe[IO, Producer, ProducerRes] =
      KafkaProducer.pipe(producerSettings)

    val stream = Stream(records)
      .through(producerStream)

//    val stream2 =
//      KafkaProducer.stream(producerSettings)
//        .flatMap { producer =>
//          KafkaConsumer.stream(consumerSettings)
//            .subscribeTo("topic")
//            .records
//            .map { committable =>
//              val key = committable.record.key
//              val value = committable.record.value
//              val record = ProducerRecord("topic", key, value)
//              ProducerRecords.one(record, committable.offset)
//            }
//            .evalMap(producer.produce)
//            .groupWithin(500, 15.seconds)
//            .evalMap(x => x.sequence)
//        }

    stream.compile.drain.as(ExitCode.Success)
  }
}
