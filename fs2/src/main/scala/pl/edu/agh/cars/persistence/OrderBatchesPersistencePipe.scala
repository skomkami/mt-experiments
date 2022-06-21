package pl.edu.agh.cars.persistence

import cats.effect.IO
import performancetest.BATCH_ERROR
import performancetest.STOP_AT_ID
import pl.edu.agh.common.{EntityStore, OrdersStore}
import pl.edu.agh.config.DbConfig
import pl.edu.agh.fs2.pipeline.{
  Input,
  KafkaInput,
  Output,
  PostgresOutput,
  StatelessPipe
}
import pl.edu.agh.model.{JsonDeserializable, OrdersBatch}

case class OrderBatchesPersistencePipe(dbConfig: DbConfig)
    extends StatelessPipe[OrdersBatch, OrdersBatch] {
  override def name: String = "fs2-orders-batches-persistence"

  override def onEvent(event: OrdersBatch): OrdersBatch = {
    event
  }

  override def input: Input[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch](
      "fs2_orders_batch",
      name,
      r => r.orders.last.id == STOP_AT_ID - BATCH_ERROR
    )
  }

  override def output: Output[OrdersBatch] =
    PostgresOutput[OrdersBatch](
      dbConfig,
      new OrdersStore[IO](_) with EntityStore[IO, OrdersBatch] {}
    )
}
