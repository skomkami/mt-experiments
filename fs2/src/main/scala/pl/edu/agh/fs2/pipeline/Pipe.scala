package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.{Stream, Pipe => FS2Pipe}
import utils.SeedScan._

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

abstract class StatefulPipe[In, S] extends Pipe[In, S] {

  def onEvent(oldState: S, event: In): S

  def onInit(event: In): S

  def restore: IO[Option[S]]

  def run: IO[_] = {
    restore
      .map {
        case Some(restored) =>
          input.source.scan(restored)(onEvent)
        case None =>
          input.source.seedScan(onInit)(onEvent)
      }
      .flatMap(_.through(output.sink).compile.drain)
  }
}
