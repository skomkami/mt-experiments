package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.{Stream, Pipe => FS2Pipe}

trait Input[T] {
  def source: Stream[IO, T]
}

trait Output[T] {
  def sink: FS2Pipe[IO, T, _]
}

trait Pipe[In, Out] {
  def input: Input[In]
  def output: Output[Out]

  def run: IO[_]
}

abstract class StatelessPipe[In, Out] extends Pipe[In, Out] {
  def onEvent(event: In): Out

  def run: IO[_] = {
    input.source.map(onEvent).through(output.sink).compile.drain
  }
}
