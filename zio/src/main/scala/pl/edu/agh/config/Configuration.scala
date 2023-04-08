package pl.edu.agh.config

import pureconfig.ConfigSource
import zio.Task
import zio.ZIO
import zio.ZLayer
import pureconfig.*
import pureconfig.generic.derivation.default.*

case class Configuration():
  val load: Task[Config] = ZIO.attempt(ConfigSource.default.loadOrThrow[Config])
object Configuration:

  val load: ZIO[Configuration, Throwable, Config] =
    ZIO.serviceWithZIO.apply(_.load)

  val live: ZLayer[Any, Nothing, Configuration] =
    ZLayer.succeed(Configuration())



