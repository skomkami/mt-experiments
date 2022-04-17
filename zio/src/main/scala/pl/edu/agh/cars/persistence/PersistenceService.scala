package pl.edu.agh.cars.persistence

import doobie.Transactor
import izumi.reflect.Tag
import pl.edu.agh.cars.persistence.persistence.Persistence
import pl.edu.agh.config.{DbConfig, configuration}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
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
  ): ZManaged[Any, Throwable, Persistence.Service[T]] = {
    lazy val transactor: Transactor[Task] = Transactor.fromDriverManager[Task](
      driver = config.driver,
      url = config.url,
      user = config.user,
      pass = config.password
    )
    ZManaged.succeed(transactor).map(mkStore)
  }

  type Persistence[T] = Has[Persistence.Service[T]]

  def live[T: Tag](
    mkStore: Transactor[Task] => Persistence.Service[T]
  ): ZLayer[configuration.Configuration, Throwable, Persistence[T]] =
    ZLayer.fromManaged(
      (for {
        config <- configuration.Configuration.load.toManaged_
        managed <- mkTransactor(config.dbConfig, mkStore)
      } yield
        managed).provideSomeLayer[configuration.Configuration](Blocking.live)
    )
}
