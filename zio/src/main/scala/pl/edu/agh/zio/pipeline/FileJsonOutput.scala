package pl.edu.agh.zio.pipeline

import io.circe.generic.encoding.DerivedAsObjectEncoder
import izumi.reflect.Tag
import pl.edu.agh.model.JsonSerializable
import record.ProcessingRecord
import zio.Chunk
import zio.stream.ZSink
import zio.stream.ZTransducer

import java.nio.file.Paths

class FileJsonOutput[T: DerivedAsObjectEncoder: Tag](path: String)(
  implicit encoder: JsonSerializable[T]
) extends Output[T] {
  override def sink: ZSink[Any, _, ProcessingRecord[T], _, _] =
    ZTransducer
      .identity[ProcessingRecord[T]]
      .map(_.value)
      .map(encoder.toJson)
      .map(_.appendedAll("\n"))
      .map(_.getBytes("UTF-8"))
      .map(Chunk.fromArray)
      .mapChunks(_.flatten)
      .>>>(ZSink.fromFile(Paths.get(path)))
}
