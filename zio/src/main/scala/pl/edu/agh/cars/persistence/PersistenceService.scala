package pl.edu.agh.cars.persistence

import doobie.Transactor
import izumi.reflect.Tag
import pl.edu.agh.cars.persistence.persistence.Persistence
import pl.edu.agh.config.{Configuration, DbConfig}
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.rts

//final class PersistenceService(tnx: Transactor[Task])
//    extends OrdersStore[Task](tnx)
//    with Persistence.Service[OrdersBatch]

//abstract class PersistenceService[T](
//  mkStore: Transactor[Task] => Persistence.Service[T]
//) extends Persistence.Service[T]

object PersistenceService {

  def mkTransactor[T](
    config: DbConfig,
    mkStore: Transactor[Task] => Persistence.Service[T]
  ): ZIO[Any, Throwable, Persistence.Service[T]] = {
    lazy val transactor: Transactor[Task] = Transactor.fromDriverManager[Task](
      driver = config.driver,
      url = config.url,
      user = config.user,
      pass = config.password
    )
    ZIO.attemptBlockingIO(transactor).map(mkStore)
  }

  def live[T: Tag](
    mkStore: Transactor[Task] => Persistence.Service[T]
  ): ZLayer[Configuration, Throwable, Persistence.Service[T]] =
    ZLayer.fromZIO(
      (for {
        config <- Configuration.load
        managed <- mkTransactor(config.dbConfig, mkStore)
      } yield managed)
    )
}
