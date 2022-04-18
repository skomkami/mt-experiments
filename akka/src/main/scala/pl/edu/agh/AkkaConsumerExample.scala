package pl.edu.agh

import _root_.akka.actor.ActorSystem
import _root_.akka.kafka.ConsumerSettings
import _root_.akka.kafka.Subscriptions
import _root_.akka.kafka.scaladsl.Consumer
import _root_.akka.stream.scaladsl.Keep
import _root_.akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import pl.edu.agh.msg.RandomMessage

import scala.concurrent.Await

object AkkaConsumerExample {
  implicit val system = ActorSystem("test")
  val config = system.settings.config.getConfig("akka.kafka.consumer")
  val messageDeserializer: Deserializer[RandomMessage] =
    (topic: String, data: Array[Byte]) => {
      RandomMessage.unsafeFromJson(new String(data))
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
