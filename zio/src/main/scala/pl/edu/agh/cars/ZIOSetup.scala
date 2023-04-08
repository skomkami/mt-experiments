package pl.edu.agh.cars

import pl.edu.agh.config.{Config, Configuration}
import pl.edu.agh.zio.pipeline.KafkaTopicsSetup
import zio.{Console, ExitCode, URIO, ZIO, ZIOAppDefault, ZLayer}

object ZIOSetup extends ZIOAppDefault {
  def run = {

    val layer = Configuration.live

    val run = for {
      config <- Configuration.load
      pipeline = new OrdersPipeline(config.inputFilePath)
      setup <- KafkaTopicsSetup
        .setupKafkaTopicsForPipeline(pipeline)
        .provide(ZLayer.succeed(config.flowsConfig))
    } yield setup

    run.provideLayer(layer)
  }
}
