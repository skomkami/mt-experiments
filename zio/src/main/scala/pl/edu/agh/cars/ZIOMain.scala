package pl.edu.agh.cars

import pl.edu.agh.config.Config
import pl.edu.agh.config.configuration.Configuration
import zio._
import zio.clock._
import zio.console.{Console}

import java.util.concurrent.TimeUnit

object ZIOMain extends App {
  def run(args: List[String]): URIO[Any with Console, ExitCode] = {

    val layer = Configuration.live

    val run = for {
      config <- ZIO.fromFunctionM[Configuration, Throwable, Config](_.get.load)
      _ <- currentTime(TimeUnit.MILLISECONDS)
        .tap(ms => ZIO.effect(println(s"start at $ms ms")))
      pipeline = new OrdersPipeline(config.inputFilePath)
      pipeline <- pipeline.run.provide(config.flowsConfig).onExit { _ =>
        currentTime(TimeUnit.MILLISECONDS)
          .tap(ms => ZIO.effectTotal(println(s"end at $ms ms")))
      }
    } yield pipeline

    run.provideLayer(layer ++ Console.live ++ Clock.live).exitCode
  }
}
