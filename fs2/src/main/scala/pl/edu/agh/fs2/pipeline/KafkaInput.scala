package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.kafka.{
  AutoOffsetReset,
  ConsumerSettings,
  Deserializer,
  KafkaConsumer
}
import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable

import scala.concurrent.duration.DurationInt

case class KafkaInput[T: DerivedDecoder](topic: String, consumerName: String)(
  implicit decoder: JsonDeserializable[T]
) extends Input[T] {

  val messageDeserializer = Deserializer.instance { (topic, headers, bytes) =>
    val either = decoder.fromJson(new String(bytes))
    IO.fromEither(either)
  }
  val consumerSettings =
    ConsumerSettings[IO, String, T](
      keyDeserializer = Deserializer[IO, String],
      valueDeserializer = messageDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId(s"fs2-$consumerName")
      .withCloseTimeout(1.minute)

  override def source: fs2.Stream[IO, T] =
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic)
      .records //TODO obsługa commitow
      .map(_.record.value)

}
