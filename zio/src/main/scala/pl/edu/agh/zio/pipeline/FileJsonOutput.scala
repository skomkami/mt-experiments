package pl.edu.agh.zio.pipeline

import io.circe.Encoder
import pl.edu.agh.model.JsonCodec
import record.ProcessingRecord
import zio.stream.{ZPipeline, ZSink}
import zio.Chunk
import java.nio.file.Paths

class FileJsonOutput[T: Encoder](path: String) extends Output[T] {
  override def sink: ZSink[Any, _, ProcessingRecord[T], _, _] =
    ZPipeline
      .identity[ProcessingRecord[T]]
      .map(_.value)
      .map(JsonCodec.toJson[T](_)(summon[Encoder[T]]))
      .map(_.appendedAll("\n"))
      .map(_.getBytes("UTF-8"))
      .map(Chunk.fromArray)
      .mapChunks(_.flatten)
      .>>>(ZSink.fromPath(Paths.get(path)))

}
