package pl.edu.agh.common

import cats.FlatMap
import cats.effect.MonadCancelThrow
import doobie.ConnectionIO
import doobie.util.update.Update
import pl.edu.agh.common.OrdersStore.SQL
import pl.edu.agh.model.{OrdersBatch, ProcessedOrder}
import cats.syntax.flatMap.*
import doobie.Transactor
import doobie.implicits.*

abstract class OrdersStore[F[_]: MonadCancelThrow: FlatMap](
  tnx: Transactor[F]
) {
  def save(ordersBatch: OrdersBatch): F[Int] = {
    val saveBuyers = SQL.insertBuyers(ordersBatch.orders).transact(tnx)
    val saveOrders = SQL.insertOrders(ordersBatch.orders).transact(tnx)
    val saveItems = SQL.insertItems(ordersBatch.orders).transact(tnx)
    saveBuyers >> saveOrders >> saveItems
  }
}

object OrdersStore {
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
}
