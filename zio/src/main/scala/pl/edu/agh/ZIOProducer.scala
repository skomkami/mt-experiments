package pl.edu.agh

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import pl.edu.agh.generator.Generator
import pl.edu.agh.msg.RandomMessage
import zio._
import zio.blocking.Blocking
import zio.duration.durationInt
import zio.json._
import zio.kafka.consumer._
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serde
import zio.stream._

import java.util.UUID
import scala.util.Failure
import scala.util.Success

object ZIOProducer extends zio.App {
  val consumerSettings: ConsumerSettings =
    ConsumerSettings(List("localhost:9092"))
      .withGroupId("zio-consumer")

  val managedConsumer = Consumer.make(consumerSettings)

  val consumer = ZLayer.fromManaged(managedConsumer)

  val messageSerde: Serde[Any, RandomMessage] = Serde.string.inmapM {
    messageAsString =>
      ZIO.fromEither(
        RandomMessage
          .fromJson(messageAsString)
          .left
          .map(new RuntimeException(_))
      )
  } { messageAsObj =>
    ZIO.effect(RandomMessage.toJson(messageAsObj))
  }

  val messageStream =
    Consumer
      .subscribeAnd(Subscription.topics("messages"))
      .plainStream(Serde.string, messageSerde.asTry)
      .map(cr => cr.record.value() -> cr.offset)
      .tap {
        case (Success(record), _) =>
          console.putStrLn(s"| x: ${record.x}, y: ${record.y} |")
        case (Failure(err), _) => console.putStrLn(s"error: ${err.getMessage}")
      }
      .map { case (_, offset) => offset }
      .aggregateAsync(Consumer.offsetBatches)
      .run(ZSink.foreach(_.commit))

  val producerSettings: ProducerSettings = ProducerSettings(
    List("localhost:9092")
  )

  val producer
    : ZLayer[Blocking, Throwable, Producer[Any, UUID, RandomMessage]] =
    ZLayer.fromManaged(
      Producer.make[Any, UUID, RandomMessage](
        producerSettings,
        Serde.uuid,
        messageSerde
      )
    )

  val randomMessage: RandomMessage = Generator.generateMsg()
  val messagesToSend: ProducerRecord[UUID, RandomMessage] =
    new ProducerRecord(
      "updates",
      UUID.fromString("b91a7348-f9f0-4100-989a-cbdd2a198096"),
      randomMessage
    )

  val producerEffect: RIO[Producer[Any, UUID, RandomMessage], RecordMetadata] =
    Producer.produce[Any, UUID, RandomMessage](messagesToSend)

  def run(args: List[String]) = {
//    val program = for {
//      _ <- ZIO.sleep(2.seconds) *> messageStream
//        .provideSomeLayer(consumer ++ zio.console.Console.live)
//        .fork
//      _ <- producerEffect.provideSomeLayer(producer) *> ZIO.sleep(5.seconds)
//    } yield ()
    val program =
      messageStream.provideSomeLayer(consumer ++ zio.console.Console.live)
    program.run.exitCode

  }
}
