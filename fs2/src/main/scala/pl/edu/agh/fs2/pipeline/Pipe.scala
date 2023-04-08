package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.{Stream, Pipe => FS2Pipe}
import pl.edu.agh.config.FlowsConfig
import record.ProcessingRecord
import utils.SeedScan.*

trait Input[T] {
  def source(partitions: Set[Int],
             partitionsCount: Int): Stream[IO, ProcessingRecord[T]]
}

trait Output[T] {
  def sink: FS2Pipe[IO, ProcessingRecord[T], Any]
}

trait Pipe[In, Out] {
  def input: Input[In]
  def output: Output[Out]

  def name: String
  def run(flowsConfig: FlowsConfig): IO[_]
}

abstract class StatelessPipe[In, Out] extends Pipe[In, Out] {
  def onEvent(event: In): Out

  def run(flowsConfig: FlowsConfig): IO[_] = {
    val partitionAssignment = flowsConfig.partitionAssignment
    Stream
      .emits(partitionAssignment)
      .map {
        case (_, partitions) =>
          input
            .source(partitions, flowsConfig.partitionsCount)
            .map(_.map(onEvent))
            .through(output.sink)
      }
      .parJoin(flowsConfig.parallelism)
      .compile
      .drain
  }
}

abstract class StatefulPipe[In, S] extends Pipe[In, S] {

  def onEvent(oldState: S, event: In): S

  def onInit(event: In): S

  def restore(partition: Int): IO[Option[S]]

  private def onRecord(oldState: ProcessingRecord[S],
                       record: ProcessingRecord[In]): ProcessingRecord[S] = {
    record.map(onEvent(oldState.value, _))
  }

  def run(flowsConfig: FlowsConfig): IO[_] = {
    val partitionAssignment = flowsConfig.partitionAssignment
    Stream
      .emits(partitionAssignment)
      .map {
        case (_, partitions) =>
          Stream
            .emits(partitions.toSeq)
            .mapAsync[IO, (Int, Option[S])](partitions.size)(
              p => restore(p).map(restored => p -> restored)
            )
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
            .map(_.through(output.sink))
            .parJoin(partitions.size)
      }
      .parJoin(flowsConfig.parallelism)
      .compile
      .drain
  }
}
