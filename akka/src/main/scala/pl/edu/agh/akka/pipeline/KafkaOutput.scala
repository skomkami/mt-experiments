package pl.edu.agh.akka.pipeline

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Sink}
import io.circe.generic.encoding.DerivedAsObjectEncoder
import io.circe.syntax.EncoderOps
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}
import pl.edu.agh.AkkaProducerExample.config
import pl.edu.agh.model.JsonSerializable

import scala.concurrent.{ExecutionContext, Future}

case class KafkaOutput[T: DerivedAsObjectEncoder](topic: String)(
  implicit encoder: JsonSerializable[T],
  actorSystem: ActorSystem
) extends Output[T] {
  private val messageSerializer: Serializer[T] =
    (_: String, data: T) => {
      data.asJson.noSpaces.getBytes("UTF-8")
    }

  private val producerSettings = ProducerSettings[String, T](
    config,
    Some(new StringSerializer),
    Some(messageSerializer)
  ).withBootstrapServers("localhost:9092")

  override def sink: Sink[T, Future[Done]] = {
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    Flow[T]
      .map(value => new ProducerRecord[String, T](topic, value))
      .to(Producer.plainSink(producerSettings))
      .mapMaterializedValue(_ => Future(Done.done()))
  }

}
