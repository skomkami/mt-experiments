package pl.edu.agh.zio.pipeline
import record.ProcessingRecord
import zio.{Scope, Task, ZIO}
import zio.stream.ZStream

case class FileInput(path: String) extends Input[String] {
  override def source(
    partitions: Set[Int],
    partitionCount: Int
  ): ZStream[Any, _, ProcessingRecord[String]] =
    ZStream
      .fromIteratorScoped(
        ZIO
          .fromAutoCloseable(ZIO.attemptBlockingIO(scala.io.Source.fromFile(path)))
          .map(_.getLines())
      )
      .filter(_.nonEmpty)
      .zipWithIndex
      .map { case (r, i) => r -> (i % partitionCount.toLong).toInt }
      .filter {
        case (_, i) => partitions.contains(i)
      }
      .map((ProcessingRecord.partitioned[String] _).tupled)
}
