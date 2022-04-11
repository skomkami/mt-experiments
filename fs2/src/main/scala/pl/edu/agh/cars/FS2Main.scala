package pl.edu.agh.cars

import cats.effect.{ExitCode, IO, IOApp}

object FS2Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    FS2OrdersPipe.run.as(ExitCode.Success)
  }
}
