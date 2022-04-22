package pl.edu.agh.zio.pipeline

import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.clients.producer.ProducerRecord
import pl.edu.agh.model.JsonDeserializable
import zio.ZIO
import zio.ZLayer
import zio.ZLayer
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde._
import org.apache.kafka.clients.producer.ProducerRecord
import zio.Chunk
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.OffsetBatch
import zio.kafka.consumer.Subscription
import zio.kafka.producer.Producer
import zio.kafka.serde.Deserializer
import zio.kafka.serde.Serde
import zio.stream.ZStream

import scala.util.Failure
import scala.util.Success

case class KafkaInput[T: DerivedDecoder](topic: String, consumerName: String)(
  implicit decoder: JsonDeserializable[T]
) extends Input[T] {

  val consumerSettings: ConsumerSettings =
    ConsumerSettings(List("localhost:9092"))
      .withGroupId(s"zio-$consumerName")

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
//      .tap {
//        case (Success(record), _) =>
//          console.putStrLn(record.toString)
//        case (Failure(err), _) => console.putStrLn(s"error: ${err.getMessage}")
//      }
      .collect {
        case Success(v) => v
      }

  private val layer = (zio.blocking.Blocking.live ++ zio.clock.Clock.live) >>> (consumer ++ zio.console.Console.live)

  override def source: ZStream[Any, _, T] =
    messageStream.provideLayer(layer)
}
