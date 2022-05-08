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
import pl.edu.agh.model.JsonSerializable
import record.ProcessingRecord

import scala.concurrent.{ExecutionContext, Future}

case class KafkaOutput[T: DerivedAsObjectEncoder](topic: String)(
  implicit encoder: JsonSerializable[T],
  actorSystem: ActorSystem
) extends OutputWithOffsetCommit[T] {
  private val messageSerializer: Serializer[T] =
    (_: String, data: T) => {
      data.asJson.noSpaces.getBytes("UTF-8")
    }

  val config = as.settings.config.getConfig("akka.kafka.producer")

  private val producerSettings = ProducerSettings[String, T](
    config,
    Some(new StringSerializer),
    Some(messageSerializer)
  ).withBootstrapServers("localhost:9092")

  override def elementSink: Sink[ProcessingRecord[T], Future[Done]] = {
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    Flow[ProcessingRecord[T]]
      .map { record =>
        val partition =
          Integer.valueOf(record.meta.map(_.partition).getOrElse(0))
        new ProducerRecord[String, T](topic, partition, null, record.value)
      }
      .to(Producer.plainSink(producerSettings))
      .mapMaterializedValue(_ => Future(Done.done()))
  }

}
