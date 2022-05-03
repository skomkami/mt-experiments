package pl.edu.agh.zio.pipeline

import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable
import zio.{Chunk, ZIO, ZLayer}
import zio.kafka.consumer.{
  Consumer,
  ConsumerSettings,
  OffsetBatch,
  Subscription
}
import zio.kafka.serde.{Deserializer, Serde}
import zio.stream.ZStream

import scala.util.Success

case class KafkaInput[T: DerivedDecoder](topic: String, consumerName: String)(
  implicit decoder: JsonDeserializable[T]
) extends Input[T] {

  val consumerSettings: ConsumerSettings =
    ConsumerSettings(List("localhost:9092"))
      .withGroupId(consumerName)

  val managedConsumer = Consumer.make(consumerSettings)

  val consumer = ZLayer.fromManaged(managedConsumer)

  val messageSerde: Deserializer[Any, T] = Deserializer.string.mapM {
    messageAsString =>
      ZIO.fromEither(
        decoder
          .fromJson(messageAsString)
          .left
          .map(new RuntimeException(_))
      )
  }

  val messageStream =
    Consumer
      .subscribeAnd(Subscription.topics(topic))
      .plainStream(Serde.string, messageSerde.asTry)
      .map(cr => cr.record.value() -> cr.offset)
      .mapChunksM { chunk =>
        val records = chunk.map(_._1)
        val offsetBatch = OffsetBatch(chunk.map(_._2))

        offsetBatch.commit.as(Chunk(())) *> ZIO.succeed(records)
      }
      .collect {
        case Success(v) => v
      }

  private val layer = (zio.blocking.Blocking.live ++ zio.clock.Clock.live) >>> (consumer ++ zio.console.Console.live)

  override def source: ZStream[Any, _, T] =
    messageStream.provideLayer(layer)
}
