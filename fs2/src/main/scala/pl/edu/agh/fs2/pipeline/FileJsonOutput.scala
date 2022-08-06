package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.Stream
import fs2.io.file.Files
import io.circe.generic.encoding.DerivedAsObjectEncoder
import pl.edu.agh.model.JsonSerializable
import record.ProcessingRecord

import java.nio.file.Paths

case class FileJsonOutput[T: DerivedAsObjectEncoder](path: String)(
  implicit encoder: JsonSerializable[T]
) extends OutputWithOffsetCommit[T] {

  override def elementSink: fs2.Pipe[IO, ProcessingRecord[T], _] =
    (s1: fs2.Stream[IO, ProcessingRecord[T]]) =>
      s1.map(_.value)
        .map(encoder.toJson)
        .map(_.appendedAll("\n"))
        .flatMap(str => Stream(str.getBytes("UTF-8"): _*))
        .through(Files[IO].writeAll(Paths.get(path)))

}
