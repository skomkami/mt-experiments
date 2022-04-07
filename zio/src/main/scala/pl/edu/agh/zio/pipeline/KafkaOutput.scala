package pl.edu.agh.zio.pipeline

import io.circe.generic.encoding.DerivedAsObjectEncoder
import izumi.reflect.Tag
import org.apache.kafka.clients.producer.ProducerRecord
import pl.edu.agh.model.JsonSerializable
import zio.ZIO
import zio.ZLayer
import zio.blocking.Blocking
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serializer
import zio.stream.ZSink

case class KafkaOutput[T: DerivedAsObjectEncoder: Tag](topic: String)(
  implicit encoder: JsonSerializable[T]
) extends Output[T] {
  val producerSettings: ProducerSettings = ProducerSettings(
    List("localhost:9092")
  )

  val messageSerializer: Serializer[Any, T] = Serializer.string.contramapM {
    messageAsObj =>
      ZIO.effect(encoder.toJson(messageAsObj))
  }

  private val managedProducer =
    Producer.make[Any, String, T](
      producerSettings,
      Serializer.string,
      messageSerializer
    )
  private val producer: ZLayer[Blocking, Throwable, Producer[Any, String, T]] =
    ZLayer.fromManaged(managedProducer)

  override def sink: ZSink[Any, _, T, _, _] =
    ZSink.foreach { (msg: T) =>
      val record = new ProducerRecord[String, T](topic, msg)
      val producerEffect = Producer.produce[Any, String, T](record)
      producerEffect
        .provideLayer(producer)
        .provideLayer(Blocking.live)
    }
}
