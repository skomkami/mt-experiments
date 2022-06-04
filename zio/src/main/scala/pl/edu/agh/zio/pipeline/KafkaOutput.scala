package pl.edu.agh.zio.pipeline

import io.circe.generic.encoding.DerivedAsObjectEncoder
import izumi.reflect.Tag
import org.apache.kafka.clients.producer.ProducerRecord
import pl.edu.agh.model.JsonSerializable
import record.ProcessingRecord
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.blocking.Blocking
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import zio.kafka.serde.Serializer
import zio.stream.ZSink

case class KafkaOutput[T: DerivedAsObjectEncoder: Tag](topic: String)(
  implicit encoder: JsonSerializable[T]
) extends OutputWithOffsetCommit[T] {
  val producerSettings: ProducerSettings = ProducerSettings(
    List("localhost:9092")
  ).withProperty("request.timeout.ms", "300000")

  val messageSerializer: Serializer[Any, T] = Serializer.string.contramapM {
    messageAsObj =>
      ZIO.effectTotal(encoder.toJson(messageAsObj))
  }

  private val managedProducer =
    Producer.make[Any, String, T](
      producerSettings,
      Serializer.string,
      messageSerializer
    )
  private val producer: ZLayer[Blocking, Throwable, Producer[Any, String, T]] =
    ZLayer.fromManaged(managedProducer)

  override def outputEffect(record: ProcessingRecord[T]): Task[_] = {
    val partition = record.meta.map(_.partition).getOrElse(0)
    println(s"output partition: $partition")
    val pr = new ProducerRecord[String, T](topic, partition, null, record.value)
    val producerEffect = Producer.produce[Any, String, T](pr)
    producerEffect
      .provideLayer(producer)
      .provideLayer(Blocking.live)
  }
}
