package pl.edu.agh.cars.batcher

import cats.effect.IO
import fs2.Stream
import pl.edu.agh.config.FlowsConfig
import pl.edu.agh.fs2.pipeline.utils.GroupUntil.GroupUntilOps
import pl.edu.agh.fs2.pipeline.FileJsonInput
import pl.edu.agh.fs2.pipeline.FileJsonOutput
import pl.edu.agh.fs2.pipeline.Input
import pl.edu.agh.fs2.pipeline.Output
import pl.edu.agh.fs2.pipeline.Pipe
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.model.ProcessedOrder
import record.ProcessingRecord

case class OrdersBatcher() extends Pipe[ProcessedOrder, OrdersBatch] {
  override def name: String = "fs2-orders-batcher"

  override def input: Input[ProcessedOrder] = {
    implicit val decoder: JsonDeserializable[ProcessedOrder] = ProcessedOrder
    FileJsonInput[ProcessedOrder]("fs2_processed_orders")
  }

  override def output: Output[OrdersBatch] = {
    implicit val decoder: JsonSerializable[OrdersBatch] = OrdersBatch
    FileJsonOutput[OrdersBatch]("fs2_orders_batch")
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
