package pl.edu.agh.akka.pipeline

import akka.Done
import akka.actor.ActorSystem
import pl.edu.agh.config.{Config, FlowsConfig}

import scala.concurrent.Future

case class Pipeline(private val pipes: List[Pipe[_, _]], config: FlowsConfig)(
  implicit val as: ActorSystem
) {
  def run: Future[Done] = {
    if (!config.isValid) {
      throw new Exception("Invalid flows config")
    }
    pipes
      .map(_.run(config))
      .foldLeft(Future.successful(Done.done()))((_, b) => b)
  }
}
