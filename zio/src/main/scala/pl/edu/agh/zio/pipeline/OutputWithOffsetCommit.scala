package pl.edu.agh.zio.pipeline

import record.ProcessingRecord
import zio.Task
import zio.stream.ZSink
import zio.stream.ZTransducer

abstract class OutputWithOffsetCommit[T] extends Output[T] {

  private val commitSink: ZSink[Any, _, ProcessingRecord[T], _, _] = {
    ZSink.foreach { (record: ProcessingRecord[T]) =>
      record.meta match {
        case Some(meta: KafkaRecordMeta) => meta.committableOffset.commit
        case _                           => Task.unit
      }
    }
  }

  final override def sink: ZSink[Any, _, ProcessingRecord[T], _, _] =
    ZTransducer
      .identity[ProcessingRecord[T]]
      .mapM(r => outputEffect(r).map(_ => r))
      .>>>(commitSink)

  def outputEffect(element: ProcessingRecord[T]): Task[_]
}
