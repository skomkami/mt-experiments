package pl.edu.agh.zio.pipeline

import io.circe.Decoder
import pl.edu.agh.model.JsonCodec
import record.ProcessingRecord
import zio.ZIO
import zio.stream.ZStream

case class FileJsonInput[T: Decoder](path: String) extends Input[T] {
  override def source(
    partitions: Set[Int],
    partitionsCount: Int
  ): ZStream[Any, _, ProcessingRecord[T]] =
    ZStream
      .fromIteratorScoped(
        ZIO
          .fromAutoCloseable(ZIO.attemptBlockingIO(scala.io.Source.fromFile(path)))
          .map(_.getLines())
      )
      .filter(_.nonEmpty)
      .map(JsonCodec.fromJson(_))
      .map(ProcessingRecord.partitioned[T](_, 0))
}
