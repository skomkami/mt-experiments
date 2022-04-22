package pl.edu.agh.akka.pipeline

import _root_.akka.actor.ActorSystem
import _root_.akka.kafka.{ConsumerSettings, Subscriptions}
import _root_.akka.kafka.scaladsl.Consumer
import _root_.akka.stream.scaladsl.Source
import akka.kafka.CommitterSettings
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Committer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import pl.edu.agh.AkkaConsumerExample.config
import pl.edu.agh.model.JsonDeserializable

case class KafkaInput[T: DerivedDecoder](topic: String, consumerName: String)(
  implicit decoder: JsonDeserializable[T],
  actorSystem: ActorSystem
) extends Input[T] {

  private val messageDeserializer: Deserializer[T] =
    (topic: String, data: Array[Byte]) => {
      decoder.unsafeFromJson(new String(data))
    }

  private val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, messageDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId(consumerName)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private val committerSettings = CommitterSettings(actorSystem)

  private val commiterSink: Sink[CommittableMessage[String, T], _] =
    Flow[CommittableMessage[String, T]]
      .map(_.committableOffset)
      .toMat(Committer.sink(committerSettings))(Keep.none)

  override def source: Source[T, _] =
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic))
      .alsoTo(commiterSink)
      .map(cr => cr.record.value())
}
