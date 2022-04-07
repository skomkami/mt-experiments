package pl.edu.agh.zio.pipeline
import zio.Task
import zio.ZManaged
import zio.stream.ZStream

case class FileInput(path: String) extends Input[String] {
  override def source: ZStream[Any, _, String] = ZStream.fromIteratorManaged(
    ZManaged
      .fromAutoCloseable(Task(scala.io.Source.fromFile(path)))
      .map(_.getLines())
  )
}
