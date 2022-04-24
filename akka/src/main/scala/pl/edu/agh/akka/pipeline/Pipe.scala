package pl.edu.agh.akka.pipeline

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import pl.edu.agh.akka.pipeline.util.SeedScan.SeedScanOps

import scala.concurrent.{ExecutionContext, Future}

trait Input[T] {
  def source: Source[T, _]
}

trait Output[T] {
  def sink: Sink[T, Future[Done]]
}

trait Pipe[In, Out] {
  def input: Input[In]
  def output: Output[Out]

  def run(implicit mat: Materializer): Future[Done]
}

abstract class StatelessPipe[In, Out] extends Pipe[In, Out] {
  def onEvent(event: In): Out

  def run(implicit mat: Materializer): Future[Done] = {
    input.source.map(onEvent).runWith(output.sink)
  }
}

abstract class StatefulPipe[In, S] extends Pipe[In, S] {

  def onEvent(oldState: S, event: In): S

  def onInit(event: In): S

  def restore: Future[Option[S]]

  def run(implicit mat: Materializer): Future[Done] = {
    implicit val ec: ExecutionContext = mat.executionContext
    restore
      .map {
        case Some(restored) =>
          input.source.scan(restored)(onEvent)
        case None =>
          input.source.seedScan(onInit)(onEvent)
      }
      .flatMap(x => x.runWith(output.sink))

  }
}
