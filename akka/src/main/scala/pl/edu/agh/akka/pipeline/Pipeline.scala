package pl.edu.agh.akka.pipeline

import akka.Done
import akka.actor.ActorSystem

import scala.concurrent.Future

case class Pipeline(private val pipes: List[Pipe[_, _]])(
  implicit val as: ActorSystem
) {
  def run: Future[Done] = {
    pipes.map(_.run).foldLeft(Future.successful(Done.done()))((_, b) => b)
  }
}
