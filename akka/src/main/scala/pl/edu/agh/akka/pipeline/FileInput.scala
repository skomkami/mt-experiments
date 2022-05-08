package pl.edu.agh.akka.pipeline

import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString
import record.ProcessingRecord

import java.nio.file.Paths

case class FileInput(path: String) extends Input[String] {
  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): Source[ProcessingRecord[String], _] =
    FileIO
      .fromPath(Paths.get(path))
      .via(
        Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 256,
          allowTruncation = true
        )
      )
      .zipWithIndex
      .map { case (r, i) => r -> (i % partitionCount.toLong).toInt }
      .filter {
        case (_, i) => partitions.contains(i)
      }
      .map { case (bs, i) => bs.utf8String -> i }
      .map((ProcessingRecord.partitioned[String] _).tupled)
}
