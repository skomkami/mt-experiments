package pl.edu.agh.zio.pipeline

import zio.ZIO
import zio.stream.ZSink
import zio.stream.ZStream

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
