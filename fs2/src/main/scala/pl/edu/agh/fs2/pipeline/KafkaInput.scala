package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.kafka._
import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable

import scala.concurrent.duration._

case class KafkaInput[T: DerivedDecoder](topic: String, consumerName: String)(
  implicit decoder: JsonDeserializable[T]
) extends Input[T] {

  val messageDeserializer = Deserializer.instance { (_, _, bytes) =>
    val either = decoder.fromJson(new String(bytes))
    IO.fromEither(either)
  }
  val consumerSettings =
    ConsumerSettings[IO, String, T](
      keyDeserializer = Deserializer[IO, String],
      valueDeserializer = messageDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId(consumerName)
      .withCloseTimeout(1.minute)

  private val commitOffsetsPipe
    : fs2.Pipe[IO, CommittableConsumerRecord[IO, String, T], T] =
    _.map(_.offset)
      .through(commitBatchWithin[IO](10, 500.millis)) >> fs2.Stream.empty

  override def source: fs2.Stream[IO, T] =
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic)
      .records
      .broadcastThrough[IO, T](commitOffsetsPipe, s1 => s1.map(_.record.value))
}
