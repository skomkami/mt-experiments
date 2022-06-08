package pl.edu.agh.cars

import cats.effect.{ExitCode, IO, IOApp}
import pl.edu.agh.config.Config
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object FS2Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    IO.blocking(ConfigSource.default.loadOrThrow[Config])
      .map(new FS2OrdersPipe(_))
      .flatMap(_.run)
      .as(ExitCode.Success)
  }
}
