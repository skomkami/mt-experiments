package pl.edu.agh.cars

import pl.edu.agh.config.Config
import pl.edu.agh.config.configuration.Configuration
import pl.edu.agh.zio.pipeline.KafkaTopicsSetup
import zio.{App, ExitCode, URIO, ZIO}
import zio.console.Console

object ZIOSetup extends App {
  def run(args: List[String]): URIO[Any with Console, ExitCode] = {

    val layer = Configuration.live
    val pipeline = new OrdersPipeline

    val run = for {
      config <- ZIO.fromFunctionM[Configuration, Throwable, Config](_.get.load)
      setup <- KafkaTopicsSetup
        .setupKafkaTopicsForPipeline(pipeline)
        .provide(config.flowsConfig)
    } yield setup

    run.provideLayer(layer).exitCode
  }
}
