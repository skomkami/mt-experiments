package pl.edu.agh.akka.pipeline

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.Future

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
