package pl.edu.agh

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.Subscriptions.TopicSubscription
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Keep, Sink}
import org.apache.kafka.common.serialization.{
  Deserializer,
  Serializer,
  StringDeserializer
}
import pl.edu.agh.msg.RandomMessage
import io.circe.parser._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerPartitionAssignor.Subscription
import org.apache.kafka.common.TopicPartition
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import pl.edu.agh.AkkaProducerExample.program

import scala.concurrent.Await

object AkkaConsumerExample {
  implicit val system = ActorSystem("test")
  val config = system.settings.config.getConfig("akka.kafka.consumer")
  val messageDeserializer: Deserializer[RandomMessage] =
    (topic: String, data: Array[Byte]) => {
      parse(new String(data))
        .flatMap(RandomMessage.jsonDecoder.decodeJson) match {
        case Right(value) => value
        case Left(error)  => throw new Throwable(s"Error: ${error.getMessage}")
      }
    }

  val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, messageDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("akka-consumer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val (program, streamComplete) = Consumer
    .plainSource(
      consumerSettings,
//      Subscriptions.assignmentWithOffset(
//        new TopicPartition("akka_messages", 0) -> 2L
//      )
      Subscriptions.topics("akka_messages")
    )
    .take(2)
    .map(cr => cr.value())
    .toMat(
      Sink.foreach(
        rm => println(s"Record id: ${rm.id}, (x,y) = (${rm.x}, ${rm.y})")
      )
    )(Keep.both)
    .run()

  def main(args: Array[String]): Unit = {
    Await.result(program.shutdown(), 120.seconds)
  }
}
