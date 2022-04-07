package pl.edu.agh.cars

import zio._
import zio.console.Console

object ZIOMain extends App {
  def run(args: List[String]): URIO[Any with Console, ExitCode] = {
    new OrdersPipeline().run.exitCode
  }
}
