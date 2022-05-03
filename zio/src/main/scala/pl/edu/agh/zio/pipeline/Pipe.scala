package pl.edu.agh.zio.pipeline

import zio.{IO, ZIO}
import zio.stream.ZSink
import zio.stream.ZStream
import pl.edu.agh.zio.pipeline.utils.SeedScan._
import zio.Task

trait Input[T] {
  def source: ZStream[Any, _, T]
}

trait Output[T] {
  def sink: ZSink[Any, _, T, _, _]
}

trait Pipe[In, Out] {
  def input: Input[In]
  def output: Output[Out]

  def run: ZIO[Any, _, _]
}

abstract class StatelessPipe[In, Out] extends Pipe[In, Out] {
  def onEvent(event: In): Out

  def run: ZIO[Any, _, _] = {
    input.source.map(onEvent).run(output.sink)
  }
}

abstract class StatefulPipe[In, S] extends Pipe[In, S] {

  def onEvent(oldState: S, event: In): S

  def onInit(event: In): S

  def restore: Task[S]

  def run: ZIO[Any, _, _] = {
    restore
      .map(input.source.scan(_)(onEvent))
      .orElseSucceed(input.source.seedScan(onInit)(onEvent))
      .flatMap(_.run(output.sink))
  }
}
