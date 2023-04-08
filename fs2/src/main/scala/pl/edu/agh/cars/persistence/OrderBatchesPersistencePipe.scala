package pl.edu.agh.cars.persistence

import cats.effect.IO
import pl.edu.agh.common.{EntityStore, OrdersStore}
import pl.edu.agh.config.DbConfig
import pl.edu.agh.fs2.pipeline.{
  Input,
  KafkaInput,
  Output,
  PostgresOutput,
  StatelessPipe
}
import pl.edu.agh.model.OrdersBatch

case class OrderBatchesPersistencePipe(dbConfig: DbConfig)
    extends StatelessPipe[OrdersBatch, OrdersBatch] {
  override def name: String = "fs2-orders-batches-persistence"

  override def onEvent(event: OrdersBatch): OrdersBatch = {
    event
  }

  override def input: Input[OrdersBatch] = {
    KafkaInput[OrdersBatch]("fs2_orders_batch", name)
  }

  override def output: Output[OrdersBatch] =
    PostgresOutput[OrdersBatch](
      dbConfig,
      new OrdersStore[IO](_) with EntityStore[IO, OrdersBatch] {}
    )
}
