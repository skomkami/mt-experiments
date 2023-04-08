package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.Stream
import fs2.io.file.Files
import io.circe.Encoder
import pl.edu.agh.model.JsonCodec
import record.ProcessingRecord
import fs2.io.file.Flags
import fs2.io.file.Path

case class FileJsonOutput[T: Encoder](path: String) extends OutputWithOffsetCommit[T] {

  override def elementSink: fs2.Pipe[IO, ProcessingRecord[T], Nothing] =
    (s1: fs2.Stream[IO, ProcessingRecord[T]]) =>
      s1.map(_.value)
        .map(JsonCodec.toJson(_))
        .map(_.appendedAll("\n"))
        .flatMap(str => Stream(str.getBytes("UTF-8"): _*))
        .through(Files[IO].writeAll(Path(path), Flags.Append))

}
