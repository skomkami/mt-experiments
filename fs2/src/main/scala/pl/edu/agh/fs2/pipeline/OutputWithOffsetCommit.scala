package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.kafka.commitBatchWithin
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import record.ProcessingRecord

abstract class OutputWithOffsetCommit[T] extends Output[T] {

  private val commitOffsetsPipe: fs2.Pipe[IO, ProcessingRecord[T], T] =
    _.collect {
      case ProcessingRecord(_, Some(krm: KafkaRecordMeta)) =>
        krm.committableOffset
    }.through(commitBatchWithin[IO](10, 500.millis)) >> fs2.Stream.empty

  final override def sink: fs2.Pipe[IO, ProcessingRecord[T], Any] =
    _.broadcastThrough(commitOffsetsPipe, elementSink)

  def elementSink: fs2.Pipe[IO, ProcessingRecord[T], Any]
}
