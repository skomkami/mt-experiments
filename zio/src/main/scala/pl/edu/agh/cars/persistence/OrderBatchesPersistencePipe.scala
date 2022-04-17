package pl.edu.agh.cars.persistence

import pl.edu.agh.cars.persistence.persistence.Persistence
import pl.edu.agh.common.OrdersStore
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.zio.pipeline.Input
import pl.edu.agh.zio.pipeline.KafkaInput
import pl.edu.agh.zio.pipeline.Output
import pl.edu.agh.zio.pipeline.PostgresOutput
import pl.edu.agh.zio.pipeline.StatelessPipe
import zio.Task
import zio.interop.catz._
import zio.interop.catz.implicits.rts

case class OrderBatchesPersistencePipe()
    extends StatelessPipe[OrdersBatch, OrdersBatch] {
  override def onEvent(event: OrdersBatch): OrdersBatch = {
    event
  }

  override def input: Input[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch](
      "zio_order_batch",
      "zio-order-batches-persistence-pipe"
    )
  }

  override def output: Output[OrdersBatch] =
    PostgresOutput(
      tnx => new OrdersStore[Task](tnx) with Persistence.Service[OrdersBatch] {}
    )
}
