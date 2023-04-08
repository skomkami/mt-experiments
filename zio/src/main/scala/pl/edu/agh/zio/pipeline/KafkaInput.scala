package pl.edu.agh.zio.pipeline

import io.circe.Decoder
import pl.edu.agh.model.JsonCodec
import record.ProcessingRecord
import zio.kafka.consumer.{Consumer, ConsumerSettings, OffsetBatch, Subscription}
import zio.kafka.serde.{Deserializer, Serde}
import zio.stream.ZStream
import zio.{Chunk, ZIO, ZLayer}

import scala.util.Success

case class KafkaInput[T: Decoder](topic: String, consumerName: String) extends Input[T] {

  val consumerSettings: ConsumerSettings =
    ConsumerSettings(List("localhost:9092"))
      .withGroupId(consumerName)

  val scopedConsumer = Consumer.make(consumerSettings)

  val consumer = ZLayer.scoped(scopedConsumer)

  val messageSerde: Deserializer[Any, T] = Deserializer.string.mapM {
    messageAsString =>
      ZIO.fromEither(
        JsonCodec
          .fromJsonSafe(messageAsString)
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

  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): ZStream[Any, _, ProcessingRecord[T]] =
    messageStream(partitions).provideLayer(consumer)
}
