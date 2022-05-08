package pl.edu.agh.cars

import akka.actor.ActorSystem
import pl.edu.agh.akka.pipeline.KafkaTopicsSetup
import pl.edu.agh.config.Config
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object AkkaSetup {
  implicit val system = ActorSystem("akka-cars-setup")

  def main(args: Array[String]): Unit = {
    val config = ConfigSource.default.loadOrThrow[Config]
    val pipeline = new OrdersPipeline(config)
    val future = KafkaTopicsSetup.setupKafkaTopicsForPipeline(pipeline)
    Await.result(future, 10.seconds)
  }
}
