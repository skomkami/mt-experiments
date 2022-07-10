package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.kafka.commitBatchWithin
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import record.ProcessingRecord
abstract class OutputWithOffsetCommit[T](
  val shutdownWhen: T => Boolean = (_: T) => false
) extends Output[T] {

  private val commitOffsetsPipe: fs2.Pipe[IO, ProcessingRecord[T], T] =
    _.collect {
      case ProcessingRecord(_, Some(krm: KafkaRecordMeta)) =>
        krm.committableOffset
    }.through(commitBatchWithin[IO](100, 500.millis)) >> fs2.Stream.empty

  private val stopPipe: fs2.Pipe[IO, ProcessingRecord[T], T] = _.evalTap { r =>
    if (shutdownWhen(r.value)) {
      println(s"End2: ${System.currentTimeMillis()} ms")
      IO.sleep(200.millis) >> IO.raiseError(new Throwable("It should stop!!!"))
    } else {
      IO.unit
    }
  } >> fs2.Stream.empty

  final override def sink: fs2.Pipe[IO, ProcessingRecord[T], _] =
    _.broadcastThrough(commitOffsetsPipe, elementSink, stopPipe)

  def elementSink: fs2.Pipe[IO, ProcessingRecord[T], _]
}
