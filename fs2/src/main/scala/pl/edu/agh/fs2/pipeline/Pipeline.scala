package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import pl.edu.agh.config.FlowsConfig

abstract class Pipeline(private[pipeline] val pipes: List[Pipe[_, _]],
                        config: FlowsConfig) {
  def run: IO[Unit] = {
    if (!config.isValid) {
      IO.raiseError(new Throwable("Invalid flows config"))
    } else {
      IO.parSequenceN(1)(pipes.map(_.run(config))).as(())
    }
  }
}
