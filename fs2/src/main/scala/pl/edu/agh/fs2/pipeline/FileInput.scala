package pl.edu.agh.fs2.pipeline
import cats.effect.IO
import fs2.io.file.Files

import java.nio.file.Paths

case class FileInput(filename: String) extends Input[String] {
  override def source: fs2.Stream[IO, String] =
    Files[IO]
      .readAll(Paths.get(filename), 4096)
      .through(_root_.fs2.text.utf8.decode)
      .through(_root_.fs2.text.lines)
      .filter(_.nonEmpty)
}
