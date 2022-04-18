package pl.edu.agh.akka.pipeline

import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString

import java.nio.file.Paths

case class FileInput(path: String) extends Input[String] {
  override def source: Source[String, _] =
    FileIO
      .fromPath(Paths.get(path))
      .via(
        Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 256,
          allowTruncation = true
        )
      )
      .map(_.utf8String)
}
