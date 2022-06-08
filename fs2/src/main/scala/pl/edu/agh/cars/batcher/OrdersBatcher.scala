package pl.edu.agh.cars.batcher

import cats.effect.IO
import fs2.Stream
import pl.edu.agh.config.FlowsConfig
import pl.edu.agh.fs2.pipeline.{Input, KafkaInput, KafkaOutput, Output, Pipe}
import pl.edu.agh.model.{
  JsonDeserializable,
  JsonSerializable,
  OrdersBatch,
  ProcessedOrder
}
import pl.edu.agh.fs2.pipeline.utils.GroupUntil.GroupUntilOps
import record.ProcessingRecord

case class OrdersBatcher() extends Pipe[ProcessedOrder, OrdersBatch] {

  override def input: Input[ProcessedOrder] = {
    implicit val decoder: JsonDeserializable[ProcessedOrder] = ProcessedOrder
    KafkaInput[ProcessedOrder]("fs2_processed_orders", "fs2-orders-batcher")
  }

  override def output: Output[OrdersBatch] = {
    implicit val decoder: JsonSerializable[OrdersBatch] = OrdersBatch
    KafkaOutput[OrdersBatch]("fs2_orders_batch")
  }

  override def run(flowsConfig: FlowsConfig): IO[_] = {
    val partitionAssignment = flowsConfig.partitionAssignment
    Stream
      .emits(partitionAssignment)
      .map {
        case (node, partitions) =>
          input
            .source(partitions, flowsConfig.partitionsCount)
            .groupUntil(ProcessingRecord(OrdersBatch.empty)) {
              case (ProcessingRecord(batch, _), ProcessingRecord(order, _)) =>
                batch.totalAmount + order.totalUSD <= 200000
            } { case (batch, single) => single.map(batch.value.add) }
            .through(output.sink)
      }
      .parJoin(flowsConfig.parallelism)
      .compile
      .drain
  }

}
