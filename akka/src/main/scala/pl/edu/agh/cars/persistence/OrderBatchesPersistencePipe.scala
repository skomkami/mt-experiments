package pl.edu.agh.cars.persistence

import akka.actor.ActorSystem
import cats.effect.IO
import pl.edu.agh.akka.pipeline._
import performancetest.BATCH_ERROR
import performancetest.STOP_AT_ID
import pl.edu.agh.common.{EntityStore, OrdersStore}
import pl.edu.agh.config.DbConfig
import pl.edu.agh.model.{JsonDeserializable, OrdersBatch}

case class OrderBatchesPersistencePipe(dbConfig: DbConfig)(
  implicit as: ActorSystem
) extends StatelessPipe[OrdersBatch, OrdersBatch] {
  override def name: String = "akka-order-batches-persistence-pipe"

  override def onEvent(event: OrdersBatch): OrdersBatch = {
    event
  }

  override def input: Input[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch](
      "akka_order_batches",
      name,
      r => r.orders.exists(_.id >= STOP_AT_ID - 12)
    )
  }

  override def output: Output[OrdersBatch] =
    PostgresOutput(
      dbConfig,
      tnx => new OrdersStore[IO](tnx) with EntityStore[IO, OrdersBatch] {}
    )
}
