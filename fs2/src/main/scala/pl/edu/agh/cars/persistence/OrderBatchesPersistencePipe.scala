package pl.edu.agh.cars.persistence

import cats.effect.IO
import performancetest.BATCH_ERROR
import performancetest.STOP_AT_ID
import pl.edu.agh.common.EntityStore
import pl.edu.agh.common.OrdersStore
import pl.edu.agh.config.DbConfig
import pl.edu.agh.fs2.pipeline.FileJsonInput
import pl.edu.agh.fs2.pipeline.Input
import pl.edu.agh.fs2.pipeline.Output
import pl.edu.agh.fs2.pipeline.PostgresOutput
import pl.edu.agh.fs2.pipeline.StatelessPipe
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.OrdersBatch

case class OrderBatchesPersistencePipe(dbConfig: DbConfig)
    extends StatelessPipe[OrdersBatch, OrdersBatch] {

  override def name: String = "fs2-orders-batches-persistence"

  override def onEvent(event: OrdersBatch): OrdersBatch = {
    event
  }

  override def input: Input[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    FileJsonInput[OrdersBatch]("fs2_orders_batch")
  }

  override def output: Output[OrdersBatch] =
    PostgresOutput[OrdersBatch](
      dbConfig,
      new OrdersStore[IO](_) with EntityStore[IO, OrdersBatch] {},
      r => r.orders.exists(_.id >= STOP_AT_ID - BATCH_ERROR - 12)
    )
}
