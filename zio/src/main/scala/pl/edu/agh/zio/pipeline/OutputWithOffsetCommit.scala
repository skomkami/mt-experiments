package pl.edu.agh.zio.pipeline

import record.ProcessingRecord
import zio.{ZIO, Task}
import zio.stream.{ZPipeline, ZSink}

abstract class OutputWithOffsetCommit[T] extends Output[T] {

  private val commitSink: ZSink[Any, _, ProcessingRecord[T], _, _] = {
    ZSink.foreach { (record: ProcessingRecord[T]) =>
      record.meta match {
        case Some(meta: KafkaRecordMeta) =>
          meta.committableOffset.commit
        case _ =>
          ZIO.unit
      }
    }
  }

  final override def sink: ZSink[Any, _, ProcessingRecord[T], _, _] =
    ZPipeline
      .identity[ProcessingRecord[T]]
      .mapZIO(r => outputEffect(r).map(_ => r))
      .>>>(commitSink)

  def outputEffect(element: ProcessingRecord[T]): Task[_]
}
