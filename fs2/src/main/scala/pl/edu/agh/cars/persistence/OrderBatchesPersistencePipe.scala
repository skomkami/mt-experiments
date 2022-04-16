package pl.edu.agh.cars.persistence

import cats.effect.IO
import pl.edu.agh.common.OrdersStore
import pl.edu.agh.config.DbConfig
import pl.edu.agh.fs2.pipeline.{
  EntityStore,
  Input,
  KafkaInput,
  Output,
  PostgresOutput,
  StatelessPipe
}
import pl.edu.agh.model.{JsonDeserializable, OrdersBatch}

case class OrderBatchesPersistencePipe(dbConfig: DbConfig)
    extends StatelessPipe[OrdersBatch, OrdersBatch] {

  override def onEvent(event: OrdersBatch): OrdersBatch = {
    event
  }

  override def input: Input[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch](
      "fs2_orders_batch",
      "fs2-order-batches-persistence-pipe"
    )
  }

  override def output: Output[OrdersBatch] =
    PostgresOutput[OrdersBatch](
      dbConfig,
      new OrdersStore[IO](_) with EntityStore[IO, OrdersBatch] {}
    )
}
