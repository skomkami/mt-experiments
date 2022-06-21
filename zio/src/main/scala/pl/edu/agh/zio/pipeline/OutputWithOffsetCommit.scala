package pl.edu.agh.zio.pipeline

import record.ProcessingRecord
import zio.{Chunk, Task}
import zio.console.putStrLn
import zio.console.Console
import zio.clock._
import zio.kafka.consumer.OffsetBatch
import zio.stream.ZSink
import zio.stream.ZTransducer

import java.util.concurrent.TimeUnit

abstract class OutputWithOffsetCommit[T] extends Output[T] {

  private val commitSink: ZSink[Any, _, ProcessingRecord[T], _, _] = {
    ZSink.foreachChunk { (records: Chunk[ProcessingRecord[T]]) =>
      val offsets = records.map(_.meta).collect {
        case Some(meta: KafkaRecordMeta) => meta.committableOffset
      }
      OffsetBatch(offsets).commit
    }
  }

  final override def sink: ZSink[Any, _, ProcessingRecord[T], _, _] =
    ZTransducer
      .identity[ProcessingRecord[T]]
      .mapM(r => outputEffect(r).map(_ => r))
      .mapChunksM(
        chunk =>
          currentTime(TimeUnit.MILLISECONDS)
            .map(ms => putStrLn(s"output chunk at $ms ms"))
            .provideLayer(Console.live ++ Clock.live)
            .map(_ => chunk)
      )
      .>>>(commitSink)

  def outputEffect(element: ProcessingRecord[T]): Task[_]
}
