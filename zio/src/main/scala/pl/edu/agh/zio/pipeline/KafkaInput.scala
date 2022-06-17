package pl.edu.agh.zio.pipeline

import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable
import record.ProcessingRecord
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

  def messageStream(
    partitions: Set[Int]
  ): ZStream[Consumer, Throwable, ProcessingRecord[T]] = {
    val tps: Seq[(String, Int)] = partitions.map(topic -> _).toSeq
    Consumer
      .subscribeAnd(Subscription.manual(tps: _*))
      .partitionedStream(Serde.string, messageSerde)
      .flatMapPar(partitions.size)(_._2)
      .map { record =>
        val meta = KafkaRecordMeta(record.partition, record.offset)
        ProcessingRecord(record.value, Some(meta))
      }
  }

  private val layer = (zio.blocking.Blocking.live ++ zio.clock.Clock.live) >>> consumer

  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): ZStream[Any, _, ProcessingRecord[T]] =
    messageStream(partitions).provideLayer(layer)
}
