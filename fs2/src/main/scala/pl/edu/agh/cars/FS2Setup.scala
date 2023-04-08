package pl.edu.agh.cars

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import pl.edu.agh.config.Config
import pl.edu.agh.fs2.pipeline.KafkaTopicsSetup
import pureconfig.ConfigSource
import pureconfig.generic.auto.*

object FS2Setup extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    IO.blocking(ConfigSource.default.loadOrThrow[Config])
      .map(new FS2OrdersPipe(_))
      .flatMap(KafkaTopicsSetup.setupKafkaTopicsForPipeline)
      .as(ExitCode.Success)
}
