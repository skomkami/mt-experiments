package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import doobie.*
import doobie.util.transactor.Transactor.Aux
import pl.edu.agh.common.EntityStore
import pl.edu.agh.config.DbConfig
import record.ProcessingRecord

case class PostgresOutput[T](config: DbConfig,
                             mkStore: Transactor[IO] => EntityStore[IO, T])
    extends OutputWithOffsetCommit[T] {

  private lazy val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = config.driver,
    url = config.url,
    user = config.user,
    pass = config.password
  )

  private lazy val store: EntityStore[IO, T] = mkStore(transactor)

  override def elementSink: fs2.Pipe[IO, ProcessingRecord[T], _] =
    _.mapAsync(1) { ent =>
      store.save(ent.value)
    }
}
