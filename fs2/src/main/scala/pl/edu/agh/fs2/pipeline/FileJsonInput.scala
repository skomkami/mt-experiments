package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.io.file.{Files, Flags, Path}
import io.circe.Decoder
import pl.edu.agh.model.JsonCodec
import record.ProcessingRecord

case class FileJsonInput[T: Decoder](path: String) extends Input[T] {

  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): fs2.Stream[IO, ProcessingRecord[T]] =
    Files[IO]
      .readAll(Path(path), 4096, Flags.Read)
      .through(_root_.fs2.text.utf8.decode)
      .through(_root_.fs2.text.lines)
      .filter(_.nonEmpty)
      .map(JsonCodec.fromJson(_))
      .map(ProcessingRecord.partitioned[T](_, 0))
}
