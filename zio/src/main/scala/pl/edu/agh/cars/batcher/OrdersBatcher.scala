package pl.edu.agh.cars.batcher

import pl.edu.agh.config.FlowsConfig
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.model.ProcessedOrder
import pl.edu.agh.zio.pipeline.Input
import pl.edu.agh.zio.pipeline.KafkaInput
import pl.edu.agh.zio.pipeline.KafkaOutput
import pl.edu.agh.zio.pipeline.Output
import pl.edu.agh.zio.pipeline.Pipe
import record.ProcessingRecord
import zio.ZIO
import zio.stream.{Sink, ZStream, ZTransducer}

case class OrdersBatcher() extends Pipe[ProcessedOrder, OrdersBatch] {
  override def name: String = "zio-orders-batcher"

  override def input: Input[ProcessedOrder] = {
    implicit val decoder: JsonDeserializable[ProcessedOrder] = ProcessedOrder
    KafkaInput[ProcessedOrder]("zio_processed_orders", name)
  }

  override def output: Output[OrdersBatch] = {
    implicit val decoder: JsonSerializable[OrdersBatch] = OrdersBatch
    KafkaOutput[OrdersBatch]("zio_order_batch")
  }

  override def run: ZIO[FlowsConfig, _, _] = {
    ZIO.accessM.apply { flowsConfig =>
      val partitionAssignment = flowsConfig.partitionAssignment
      ZStream
        .fromIterable(partitionAssignment)
        .mapMPar(flowsConfig.parallelism) {
          case (node, partitions) =>
            input
              .source(partitions, flowsConfig.partitionsCount)
              .aggregate(
                ZTransducer
                  .foldWeighted[
                    ProcessingRecord[ProcessedOrder],
                    ProcessingRecord[OrdersBatch]
                  ](ProcessingRecord(OrdersBatch.empty))({
                    case (_, single) => single.value.totalUSD.toInt
                  }, 200000) {
                    case (batch, single) => single.map(batch.value.add)
                  }
              )
              .run(output.sink)
        }
        .run(Sink.count)
    }
  }

}
