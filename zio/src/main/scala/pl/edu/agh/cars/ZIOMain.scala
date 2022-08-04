package pl.edu.agh.cars

import pl.edu.agh.config.Config
import pl.edu.agh.config.configuration.Configuration
import zio._
import zio.console.Console

object ZIOMain extends App {
  def run(args: List[String]): URIO[Any with Console, ExitCode] = {

    val layer = Configuration.live

    val run = for {
      config <- ZIO.fromFunctionM[Configuration, Throwable, Config](_.get.load)
      pipeline = new OrdersPipeline(
        config.inputFilePath,
        config.enabledPipelines
      )
      pipeline <- pipeline.run.provide(config.flowsConfig)
    } yield pipeline

    run.provideLayer(layer).exitCode
  }
}
