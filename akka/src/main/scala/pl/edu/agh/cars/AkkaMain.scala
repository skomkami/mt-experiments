package pl.edu.agh.cars

import akka.actor.ActorSystem
import pl.edu.agh.config.Config
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object AkkaMain {

  implicit val system = ActorSystem("akka-cars-pipeline")

  def main(args: Array[String]): Unit = {
    val config = ConfigSource.default.loadOrThrow[Config]
    println(s"Start: ${System.currentTimeMillis()} ms")
    new OrdersPipeline(config).run
    ()
  }
}
