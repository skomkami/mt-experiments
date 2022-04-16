package pl.edu.agh.cars.persistence

import doobie.Transactor
import pl.edu.agh.cars.persistence.persistence.Persistence
import pl.edu.agh.common.OrdersStore
import pl.edu.agh.config.{DbConfig, configuration}
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.interop.catz.implicits.rts

final class PersistenceService(tnx: Transactor[Task])
    extends OrdersStore[Task](tnx)
    with Persistence.Service

object PersistenceService {

  def mkTransactor(
    config: DbConfig
  ): ZManaged[Any, Throwable, PersistenceService] = {
    lazy val transactor: Transactor[Task] = Transactor.fromDriverManager[Task](
      driver = config.driver,
      url = config.url,
      user = config.user,
      pass = config.password
    )
    ZManaged.succeed(transactor).map(new PersistenceService(_))
  }

  type Persistence = Has[PersistenceService]

  val live: ZLayer[configuration.Configuration, Throwable, Persistence] =
    ZLayer.fromManaged(
      (for {
        config <- configuration.Configuration.load.toManaged_
        managed <- mkTransactor(config.dbConfig)
      } yield
        managed).provideSomeLayer[configuration.Configuration](Blocking.live)
    )
}
