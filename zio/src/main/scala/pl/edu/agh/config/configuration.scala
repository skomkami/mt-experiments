package pl.edu.agh.config

import pureconfig.ConfigSource
import zio.Task
import zio.ZIO
import zio.ZLayer
import pureconfig.generic.auto._

object configuration {
  type Configuration = zio.Has[Configuration.Service]

  object Configuration {
    trait Service {
      val load: Task[Config]
    }
    trait Live extends Configuration.Service {
      val load: Task[Config] =
        Task.effect(ConfigSource.default.loadOrThrow[Config])
    }

    val load: ZIO[Configuration, Throwable, Config] =
      ZIO.accessM(_.get.load)

    val live: ZLayer[Any, Nothing, Configuration] =
      ZLayer.succeed(new Live {})
  }

}
