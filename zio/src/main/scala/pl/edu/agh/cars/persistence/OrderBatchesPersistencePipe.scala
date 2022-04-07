package pl.edu.agh.cars.persistence

import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.zio.pipeline.Input
import pl.edu.agh.zio.pipeline.KafkaInput
import pl.edu.agh.zio.pipeline.Output
import pl.edu.agh.zio.pipeline.PostgresOutput
import pl.edu.agh.zio.pipeline.StatelessPipe

case class OrderBatchesPersistencePipe()
    extends StatelessPipe[OrdersBatch, OrdersBatch] {
  override def onEvent(event: OrdersBatch): OrdersBatch = {
    event
  }

  override def input: Input[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch]("order_batch", "order-batches-persistence-pipe")
  }

  override def output: Output[OrdersBatch] = PostgresOutput()
}
