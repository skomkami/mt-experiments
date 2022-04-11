package pl.edu.agh.fs2.pipeline

import cats.effect.IO

case class Pipeline(private val pipes: List[Pipe[_, _]]) {
  def run: IO[Unit] = {
    IO.parSequenceN(3)(pipes.map(_.run)).as(())
  }
}
