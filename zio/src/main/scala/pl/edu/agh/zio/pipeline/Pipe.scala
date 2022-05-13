package pl.edu.agh.zio.pipeline

import pl.edu.agh.config.FlowsConfig
import pl.edu.agh.zio.pipeline.utils.SeedScan._
import record.ProcessingRecord
import zio.Task
import zio.ZIO
import zio.stream.Sink
import zio.stream.ZSink
import zio.stream.ZStream

trait Input[T] {
  def source(partitions: Set[Int],
             partitionsCount: Int): ZStream[Any, _, ProcessingRecord[T]]
}

trait Output[T] {
  def sink: ZSink[Any, _, ProcessingRecord[T], _, _]
}

trait Pipe[In, Out] {
  def input: Input[In]
  def output: Output[Out]

  def run: ZIO[FlowsConfig, _, _]
}

abstract class StatelessPipe[In, Out] extends Pipe[In, Out] {
  def onEvent(event: In): Out

  def run: ZIO[FlowsConfig, _, _] = {
    ZIO.accessM.apply { flowsConfig =>
      val partitionAssignment = flowsConfig.partitionAssignment
      ZStream
        .fromIterable(partitionAssignment)
        .mapMPar(flowsConfig.parallelism) {
          case (_, partitions) =>
            input
              .source(partitions, partitions.size)
              .map(_.map(onEvent))
              .run(output.sink)
        }
        .run(Sink.count)
    }
  }
}

abstract class StatefulPipe[In, S] extends Pipe[In, S] {

  def onEvent(oldState: S, event: In): S

  def onInit(event: In): S

  def restore(partition: Int): Task[Option[S]]

  private def onRecord(oldState: ProcessingRecord[S],
                       record: ProcessingRecord[In]): ProcessingRecord[S] = {
    record.map(onEvent(oldState.value, _))
  }

  def run: ZIO[FlowsConfig, _, _] = {
    ZIO.access.apply { flowsConfig =>
      val partitionAssignment = flowsConfig.partitionAssignment
      ZStream
        .fromIterable(partitionAssignment)
        .flatMapPar(flowsConfig.parallelism) {
          case (_, partitions) =>
            ZStream
              .fromIterable(partitions)
              .mapM(p => restore(p).map(restored => p -> restored))
              .map {
                case (p, Some(restored)) =>
                  input
                    .source(Set(p), flowsConfig.partitionsCount)
                    .scan(ProcessingRecord.partitioned(restored, p))(onRecord)
                case (p, None) =>
                  input
                    .source(Set(p), flowsConfig.partitionsCount)
                    .seedScan(_.map(onInit))(onRecord)
              }
              .mapM(_.run(output.sink))
        }
        .run(Sink.count)
    }
  }
}
