package pl.edu.agh

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import io.circe.syntax.EncoderOps
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import pl.edu.agh.generator.Generator
import pl.edu.agh.msg.RandomMessage

import scala.concurrent.duration.DurationInt
import scala.concurrent.Await
import scala.concurrent.Future

object AkkaProducerExample {
  implicit val system = ActorSystem("test")
  val config = system.settings.config.getConfig("akka.kafka.producer")

  val messageSerializer: Serializer[RandomMessage] =
    (topic: String, data: RandomMessage) => {
      data.asJson.noSpaces.getBytes("UTF-8")
    }

  val producerSettings =
    ProducerSettings[String, RandomMessage](
      config,
      Some(new StringSerializer),
      Some(messageSerializer)
    ).withBootstrapServers("localhost:9092")

  lazy val program: Future[Done] =
    Source
      .single(Generator.generateMsg())
      .map(
        value =>
          new ProducerRecord[String, RandomMessage]("akka_messages", value)
      )
      .runWith(Producer.plainSink(producerSettings))

  def main(args: Array[String]): Unit = {
    Await.result(program, 5.seconds)
  }
}
