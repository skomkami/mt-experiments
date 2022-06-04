package pl.edu.agh.zio.pipeline

import _root_.zio.ZIO
import pl.edu.agh.config.FlowsConfig

case class Pipeline(private[pipeline] val pipes: List[Pipe[_, _]]) {
  def run: ZIO[FlowsConfig, _, Unit] = {
    ZIO.collectAllPar_(pipes.map(_.run))
  }
}
