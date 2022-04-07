package pl.edu.agh.cars.batcher

import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.model.ProcessedOrder
import pl.edu.agh.zio.pipeline.Input
import pl.edu.agh.zio.pipeline.KafkaInput
import pl.edu.agh.zio.pipeline.KafkaOutput
import pl.edu.agh.zio.pipeline.Output
import pl.edu.agh.zio.pipeline.Pipe
import zio.ZIO
import zio.stream.ZTransducer

case class OrdersBatcher() extends Pipe[ProcessedOrder, OrdersBatch] {

  override def input: Input[ProcessedOrder] = {
    implicit val decoder: JsonDeserializable[ProcessedOrder] = ProcessedOrder
    KafkaInput[ProcessedOrder]("processed_orders", "orders-batcher")
  }

  override def output: Output[OrdersBatch] = {
    implicit val decoder: JsonSerializable[OrdersBatch] = OrdersBatch
    KafkaOutput[OrdersBatch]("order_batch")
  }

//  override def run: ZIO[Any, _, _] =
//    input.source.foldWhile(OrdersBatch.empty)(_.totalAmount < 200000)(_ add _)
  override def run: ZIO[Any, _, _] =
    input.source
      .aggregate(
        ZTransducer.foldWeighted[ProcessedOrder, OrdersBatch](
          OrdersBatch.empty
        )((acc, order) => (acc.totalAmount + order.totalUSD).toLong, 200000)(
          _ add _
        )
      )
      .run(output.sink)

}
