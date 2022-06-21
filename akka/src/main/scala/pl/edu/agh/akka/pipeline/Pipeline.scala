package pl.edu.agh.akka.pipeline

import akka.Done
import akka.actor.ActorSystem
import pl.edu.agh.config.FlowsConfig

import scala.concurrent.{ExecutionContext, Future}

case class Pipeline(private[pipeline] val pipes: List[Pipe[_, _]],
                    config: FlowsConfig)(implicit val as: ActorSystem) {
  def run: Future[Done] = {
    if (!config.isValid) {
      throw new Exception("Invalid flows config")
    }
    implicit val ec: ExecutionContext = as.dispatcher
    val f = Future
      .sequence(
        pipes
          .map(_.run(config))
      )
      .map(_ => Done.done())
    f.onComplete(_ => as.terminate())
    f
  }
}
