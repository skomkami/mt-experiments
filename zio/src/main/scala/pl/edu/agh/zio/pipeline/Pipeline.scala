package pl.edu.agh.zio.pipeline

import _root_.zio.ZIO

case class Pipeline(private val pipes: List[Pipe[_, _]]) {
  def run: ZIO[Any, _, Unit] = {
    ZIO.collectAllPar_(pipes.map(_.run))
  }
}
