package pl.edu.agh.fs2.pipeline

import cats.effect.Clock
import cats.effect.IO
import fs2.kafka.commitBatchWithin
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import record.ProcessingRecord
abstract class OutputWithOffsetCommit[T] extends Output[T] {

  private val commitOffsetsPipe: fs2.Pipe[IO, ProcessingRecord[T], T] =
    _.collect {
      case ProcessingRecord(_, Some(krm: KafkaRecordMeta)) =>
        krm.committableOffset
    }.through(commitBatchWithin[IO](100, 500.millis)) >> fs2.Stream.empty

  private val printTimePipe: fs2.Pipe[IO, ProcessingRecord[T], T] =
    _.chunks
      .evalMap { o =>
        Clock[IO].realTime
          .map(time => IO.println(s"Output time: ${time.toMillis} ms"))
          .map(_ => o)
      }
      .map(_.head)
      .collect {
        case Some(opt) => opt.value
      }

  final override def sink: fs2.Pipe[IO, ProcessingRecord[T], _] =
    _.broadcastThrough(commitOffsetsPipe, elementSink, printTimePipe)

  def elementSink: fs2.Pipe[IO, ProcessingRecord[T], _]
}
