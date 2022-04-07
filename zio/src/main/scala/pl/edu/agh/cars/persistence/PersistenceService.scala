package pl.edu.agh.cars.persistence

import doobie.ConnectionIO
import doobie.Transactor
import doobie.implicits._
import doobie.util.update.Update
import pl.edu.agh.cars.persistence.persistence.Persistence
import pl.edu.agh.config.DbConfig
import pl.edu.agh.config.configuration
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.model.ProcessedOrder
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.interop.catz.implicits.rts
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.implicits.legacy._

import java.time.OffsetDateTime

final class PersistenceService(tnx: Transactor[Task])
    extends Persistence.Service {
  import PersistenceService._

  def saveBatch(ordersBatch: OrdersBatch): Task[Int] = {
    val saveBuyers = SQL.insertBuyers(ordersBatch.orders).transact(tnx)
    val saveOrders = SQL.insertOrders(ordersBatch.orders).transact(tnx)
    val saveItems = SQL.insertItems(ordersBatch.orders).transact(tnx)
    saveBuyers *> saveOrders *> saveItems
  }

}
object PersistenceService {

  type Buyer = (Int, String, String, String)
  type Order = (Int, String, BigDecimal)
  type Item = (Int, String, String, BigDecimal, String, BigDecimal)

  object SQL {
    def insertBuyers(ps: List[ProcessedOrder]): ConnectionIO[Int] = {
      val sql =
        "insert into buyers (id, name, address, email) values (?, ?, ?, ?)"
      val buyers = ps.map { po =>
        (po.id, po.buyer.name, po.buyer.address, po.buyer.email)
      }
      Update[Buyer](sql).updateMany(buyers)
    }

    def insertOrders(ps: List[ProcessedOrder]): ConnectionIO[Int] = {
      val sql =
        "insert into orders (id, order_date, totalUSD) values (?, ?, ?)"
      val orders = ps.map { po =>
        (po.id, po.orderDate.toString, po.totalUSD)
      }
      Update[Order](sql).updateMany(orders)
    }

    def insertItems(ps: List[ProcessedOrder]): ConnectionIO[Int] = {
      val sql =
        "insert into items (order_id, model, type, price, currency, priceUSD) values (?, ?, ?, ?, ?, ?)"
      val orders = for {
        order <- ps
        item <- order.items
      } yield {
        (
          order.id,
          item.model.toString,
          item.`type`.toString,
          item.price,
          item.currency,
          item.priceUSD
        )
      }
      Update[Item](sql).updateMany(orders)
    }
  }

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
