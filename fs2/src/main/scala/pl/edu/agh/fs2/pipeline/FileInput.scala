package pl.edu.agh.fs2.pipeline
import cats.effect.IO
import fs2.io.file.Files
import record.ProcessingRecord

import java.nio.file.Paths

case class FileInput(filename: String) extends Input[String] {
  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): fs2.Stream[IO, ProcessingRecord[String]] =
    Files[IO]
      .readAll(Paths.get(filename), 4096)
      .through(_root_.fs2.text.utf8.decode)
      .through(_root_.fs2.text.lines)
      .filter(_.nonEmpty)
      .zipWithIndex
      .map { case (r, i) => r -> (i % partitionCount.toLong).toInt }
      .filter {
        case (_, i) => partitions.contains(i)
      }
      .map((ProcessingRecord.partitioned[String] _).tupled)

}
