package pl.edu.agh.akka.pipeline

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.Transactor
import doobie.util.transactor.Transactor.Aux
import pl.edu.agh.common.EntityStore
import pl.edu.agh.config.DbConfig
import record.ProcessingRecord

import scala.concurrent.Future

case class PostgresOutput[T](
  config: DbConfig,
  mkStore: Transactor[IO] => EntityStore[IO, T]
)(implicit as: ActorSystem)
    extends OutputWithOffsetCommit[T] {

  private lazy val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = config.driver,
    url = config.url,
    user = config.user,
    pass = config.password
  )

  private lazy val store: EntityStore[IO, T] = mkStore(transactor)
  override def elementSink: Sink[ProcessingRecord[T], Future[Done]] =
    Flow[ProcessingRecord[T]]
      .mapAsync(1) { ent =>
        store
          .save(ent.value)
          .unsafeToFuture() //.map(_ => Done.done())(as.dispatcher)
      }
      .toMat(Sink.ignore)(Keep.right)
}
