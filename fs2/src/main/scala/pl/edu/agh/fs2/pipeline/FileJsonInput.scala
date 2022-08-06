package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.io.file.Files
import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable
import record.ProcessingRecord

import java.nio.file.Paths

case class FileJsonInput[T: DerivedDecoder](path: String)(
  implicit decoder: JsonDeserializable[T]
) extends Input[T] {

  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): fs2.Stream[IO, ProcessingRecord[T]] =
    Files[IO]
      .readAll(Paths.get(path), 16384)
      .through(_root_.fs2.text.utf8.decode)
      .through(_root_.fs2.text.lines)
      .filter(_.nonEmpty)
      .map(decoder.unsafeFromJson)
      .map(ProcessingRecord.partitioned[T](_, 0))
}
