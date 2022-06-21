package pl.edu.agh.akka.pipeline

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import pl.edu.agh.akka.pipeline.util.SeedScan.SeedScanOps
import pl.edu.agh.config.FlowsConfig
import record.ProcessingRecord

import scala.concurrent.{ExecutionContext, Future}

trait Input[T] {
  def source(partitions: Set[Int],
             partitionsCount: Int): Source[ProcessingRecord[T], _]
}

trait Output[T] {
  def sink: Sink[ProcessingRecord[T], Future[Done]]
}

trait Pipe[In, Out] {
  def input: Input[In]
  def output: Output[Out]

  def name: String

  def run(flowsConfig: FlowsConfig)(implicit mat: Materializer): Future[Done]
}

abstract class StatelessPipe[In, Out] extends Pipe[In, Out] {
  def onEvent(event: In): Out

  def run(
    flowsConfig: FlowsConfig
  )(implicit mat: Materializer): Future[Done] = {
    val partitionAssignment = flowsConfig.partitionAssignment
    Source(partitionAssignment)
      .mapAsyncUnordered(flowsConfig.parallelism) {
        case (_, partitions) =>
          input
            .source(partitions, flowsConfig.partitionsCount)
            .map(_.map(onEvent))
            .runWith(output.sink)
      }
      .run()
  }
}

abstract class StatefulPipe[In, S] extends Pipe[In, S] {

  def onEvent(oldState: S, event: In): S

  def onInit(event: In): S

  def restore(partition: Int): Future[Option[S]]

  private def onRecord(oldState: ProcessingRecord[S],
                       record: ProcessingRecord[In]): ProcessingRecord[S] = {
    record.map(onEvent(oldState.value, _))
  }

  def run(
    flowsConfig: FlowsConfig
  )(implicit mat: Materializer): Future[Done] = {
    implicit val ec: ExecutionContext = mat.executionContext
    val partitionAssignment = flowsConfig.partitionAssignment
    Source(partitionAssignment)
      .flatMapMerge(
        flowsConfig.parallelism, {
          case (_, partitions) =>
            Source(partitions)
              .mapAsync(1)(p => restore(p).map(restored => p -> restored))
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
              .mapAsync(1)(_.runWith(output.sink)) //TODO do sprawdzenia czy dalej działa
        }
      )
      .run()
  }
}
