package pl.edu.agh.cars

import cats.effect.{Clock, ExitCode, IO, IOApp}
import pl.edu.agh.config.Config
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object FS2Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    IO.blocking(ConfigSource.default.loadOrThrow[Config])
      .map(new FS2OrdersPipe(_))
      .flatTap(
        _ =>
          Clock[IO].realTime
            .map { time =>
              println(s"Start: ${time.toMillis} ms")
              IO(println(s"Start: ${time.toMillis} ms"))
          }
      )
      .flatMap(_.run)
      .flatTap(
        _ =>
          Clock[IO].realTime
            .map { time =>
              println(s"End: ${time.toMillis} ms")
              IO(println(s"End: ${time.toMillis} ms"))
          }
      )
      .as(ExitCode.Success)
  }
}
