package pl.edu.agh.zio.pipeline

import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable
import record.ProcessingRecord
import zio.Task
import zio.ZManaged
import zio.stream.ZStream

case class FileJsonInput[T: DerivedDecoder](path: String)(
  implicit decoder: JsonDeserializable[T]
) extends Input[T] {
  override def source(
    partitions: Set[Int],
    partitionsCount: Int
  ): ZStream[Any, _, ProcessingRecord[T]] =
    ZStream
      .fromIteratorManaged(
        ZManaged
          .fromAutoCloseable(Task(scala.io.Source.fromFile(path)))
          .map(_.getLines())
      )
      .filter(_.nonEmpty)
      .map(decoder.unsafeFromJson)
      .map(ProcessingRecord.partitioned[T](_, 0))
}
