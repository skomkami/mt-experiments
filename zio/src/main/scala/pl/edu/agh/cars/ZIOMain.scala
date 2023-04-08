package pl.edu.agh.cars

import pl.edu.agh.config.Config
import pl.edu.agh.config.Configuration
import zio.*

object ZIOMain extends ZIOAppDefault {
  def run = {

    val layer = Configuration.live

    val run = for {
      config <- Configuration.load
      pipeline = new OrdersPipeline(
        config.inputFilePath,
        config.enabledPipelines
      )
      pipeline <- pipeline.run.provideLayer(ZLayer.succeed(config.flowsConfig))
    } yield pipeline

    run.provideLayer(layer)
  }
}
