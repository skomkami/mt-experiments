package pl.edu.agh.akka.pipeline

import _root_.akka.actor.ActorSystem
import _root_.akka.kafka.scaladsl.Consumer
import _root_.akka.kafka.{ConsumerSettings, Subscriptions}
import _root_.akka.stream.scaladsl.Source
import akka.stream.Materializer
import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import pl.edu.agh.AkkaConsumerExample.system
import pl.edu.agh.model.JsonDeserializable
import record.ProcessingRecord

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class KafkaInput[T: DerivedDecoder](
  topic: String,
  consumerName: String,
  shutdownWhen: T => Boolean = (_: T) => false
)(implicit decoder: JsonDeserializable[T], actorSystem: ActorSystem)
    extends Input[T] {
  val config = system.settings.config.getConfig("akka.kafka.consumer")

  private val messageDeserializer: Deserializer[T] =
    (_: String, data: Array[Byte]) => {
      decoder.unsafeFromJson(new String(data))
    }

  private val consumerSettings =
    ConsumerSettings(config, new StringDeserializer, messageDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId(consumerName)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  override def source(partitions: Set[Int],
                      partitionCount: Int): Source[ProcessingRecord[T], _] = {
    val tps = partitions.map(p => new TopicPartition(topic, p))
    implicit val mat = Materializer(actorSystem)
    val (control, source) = Consumer
      .committableSource(consumerSettings, Subscriptions.assignment(tps))
      .preMaterialize()
    source
      .map { cr =>
        val meta = KafkaRecordMeta(cr.record.partition(), cr.committableOffset)
        record.ProcessingRecord(cr.record.value(), Some(meta))
      }
      .wireTap { r =>
        if (shutdownWhen(r.value)) {
          Await.result(control.shutdown(), Duration.Inf)
        }
      }
  }
}
