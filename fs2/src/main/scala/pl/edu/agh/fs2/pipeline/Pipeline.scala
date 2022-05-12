package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import pl.edu.agh.config.FlowsConfig

case class Pipeline(private[pipeline] val pipes: List[Pipe[_, _]],
                    flowsConfig: FlowsConfig) {
  def run: IO[Unit] = {
    if (!flowsConfig.isValid) {
      IO.raiseError(new Throwable("Invalid flows config"))
    } else {
      IO.parSequenceN(pipes.size)(pipes.map(_.run(flowsConfig))).as(())
    }
  }
}
