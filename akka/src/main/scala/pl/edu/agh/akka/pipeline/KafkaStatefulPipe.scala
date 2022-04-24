package pl.edu.agh.akka.pipeline
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import cats.implicits.toTraverseOps
import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.clients.admin.{Admin, AdminClientConfig, OffsetSpec}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import pl.edu.agh.AkkaConsumerExample.config
import pl.edu.agh.model.JsonDeserializable

import java.util.Properties
import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.Try

abstract class KafkaStatefulPipe[In, S: DerivedDecoder](
  implicit val decoder: JsonDeserializable[S],
  actorSystem: ActorSystem
) extends StatefulPipe[In, S] {
  override def input: KafkaInput[In]

  override def output: KafkaOutput[S]

  private val messageDeserializer: Deserializer[S] =
    (topic: String, data: Array[Byte]) => {
      decoder.unsafeFromJson(new String(data))
    }

  private val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, messageDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("recover")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def getCommittedOffset: Option[Long] = {
    Try {
      val properties: Properties = new Properties
      properties.put(
        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
        "localhost:9092"
      )
      val admin: Admin = Admin.create(properties)
      val tp = new TopicPartition(output.topic, 0)
      val util: Map[TopicPartition, OffsetSpec] = Map(tp -> OffsetSpec.latest())
      val offsetsRes =
        admin.listOffsets(util.asJava)
      val offsetAndMeta = offsetsRes
        .partitionResult(tp)
        .get(1, TimeUnit.SECONDS)
      offsetAndMeta.offset()
      //scribe.warn(msg.getMessage);
    }.fold(fa = msg => { None }, fb = Option.apply)
  }

  private def readMessageAtOffset(offset: Long) = {
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
      .successful(getCommittedOffset)
      .flatMap(_.traverse(readMessageAtOffset))
  }
}
