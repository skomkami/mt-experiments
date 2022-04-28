package pl.edu.agh.akka.pipeline
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.stream.scaladsl.Sink
import cats.implicits.toTraverseOps
import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import pl.edu.agh.AkkaConsumerExample.config
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.util.kafka.KafkaUtil

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

abstract class KafkaStatefulPipe[In, S: DerivedDecoder](
  implicit val decoder: JsonDeserializable[S],
  actorSystem: ActorSystem
) extends StatefulPipe[In, S] {
  override def input: KafkaInput[In]

  override def output: KafkaOutput[S]

  private val messageDeserializer: Deserializer[S] =
    (_: String, data: Array[Byte]) => {
      decoder.unsafeFromJson(new String(data))
    }

  private val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, messageDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("recover")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private def readMessageAtOffset(offset: Long): Future[S] = {
    Consumer
      .plainSource(
        consumerSettings,
        Subscriptions.assignmentWithOffset(
          new TopicPartition(output.topic, 0) -> (offset - 1)
        )
      )
      .map(_.value())
      .take(1)
      .runWith(Sink.head)
  }

  override def restore: Future[Option[S]] = {
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    Future
      .successful(
        KafkaUtil.getCommittedOffset(new TopicPartition(output.topic, 0))
      )
      .flatMap(_.traverse(readMessageAtOffset))
  }
}
