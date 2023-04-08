package pl.edu.agh.zio.pipeline

import io.circe.Encoder
import izumi.reflect.Tag
import org.apache.kafka.clients.producer.ProducerRecord
import pl.edu.agh.model.JsonCodec
import record.ProcessingRecord
import zio.{Task, ZIO, ZLayer}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serializer
import zio.stream.ZSink

case class KafkaOutput[T: Encoder](topic: String) extends OutputWithOffsetCommit[T] {
  val producerSettings: ProducerSettings = ProducerSettings(
    List("localhost:9092")
  )

  val messageSerializer: Serializer[Any, T] = Serializer.string.contramapM {
    messageAsObj =>
      ZIO.attempt(JsonCodec.toJson(messageAsObj))
  }

  private val managedProducer = Producer.make(producerSettings)

  private val producer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(managedProducer)

  override def outputEffect(record: ProcessingRecord[T]): Task[_] = {
    val partition = record.meta.map(_.partition).getOrElse(0)
    val pr = new ProducerRecord[String, T](topic, partition, null, record.value)
    val producerEffect = Producer.produce[Any, String, T](pr, Serializer.string, messageSerializer)
    producerEffect
      .provideLayer(producer)
  }
}
