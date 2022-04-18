package pl.edu.agh.akka.pipeline

import _root_.akka.actor.ActorSystem
import _root_.akka.kafka.{ConsumerSettings, Subscriptions}
import _root_.akka.kafka.scaladsl.Consumer
import _root_.akka.stream.scaladsl.Source
import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import pl.edu.agh.AkkaConsumerExample.config
import pl.edu.agh.model.JsonDeserializable

case class KafkaInput[T: DerivedDecoder](topic: String, consumerName: String)(
  implicit decoder: JsonDeserializable[T],
  actorSystem: ActorSystem
) extends Input[T] {

  val messageDeserializer: Deserializer[T] =
    (topic: String, data: Array[Byte]) => {
      decoder.unsafeFromJson(new String(data))
    }

  val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, messageDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId(consumerName)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  override def source: Source[T, _] =
    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
      .map(cr => cr.value())
}
