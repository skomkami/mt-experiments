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

import scala.concurrent.Future

case class PostgresOutput[T](
  config: DbConfig,
  mkStore: Transactor[IO] => EntityStore[IO, T]
)(implicit as: ActorSystem)
    extends Output[T] {

  private lazy val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    driver = config.driver,
    url = config.url,
    user = config.user,
    pass = config.password
  )

  private lazy val store: EntityStore[IO, T] = mkStore(transactor)
  override def sink: Sink[T, Future[Done]] =
    Flow[T]
      .mapAsync(1) { ent =>
        store.save(ent).unsafeToFuture() //.map(_ => Done.done())(as.dispatcher)
      }
      .toMat(Sink.ignore)(Keep.right)
}
