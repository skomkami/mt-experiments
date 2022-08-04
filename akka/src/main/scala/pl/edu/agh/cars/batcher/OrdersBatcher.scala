package pl.edu.agh.cars.batcher

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import pl.edu.agh.akka.pipeline.{Input, KafkaInput, KafkaOutput, Output, Pipe}
import pl.edu.agh.config.FlowsConfig
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.model.ProcessedOrder
import record.ProcessingRecord

import scala.concurrent.Future

case class OrdersBatcher()(implicit as: ActorSystem)
    extends Pipe[ProcessedOrder, OrdersBatch] {
  override def name: String = "akka-orders-batcher"

  override def input: Input[ProcessedOrder] = {
    implicit val decoder: JsonDeserializable[ProcessedOrder] = ProcessedOrder
    KafkaInput[ProcessedOrder]("akka_processed_orders", name)
  }

  override def output: Output[OrdersBatch] = {
    implicit val decoder: JsonSerializable[OrdersBatch] = OrdersBatch
    KafkaOutput[OrdersBatch]("akka_order_batches")
  }
  private val precision = 10

  override def run(
    flowsConfig: FlowsConfig
  )(implicit mat: Materializer): Future[Done] = {
    val partitionAssignment = flowsConfig.partitionAssignment
    Source(partitionAssignment)
      .mapAsyncUnordered(flowsConfig.parallelism) {
        case (_, partitions) =>
          input
            .source(partitions, flowsConfig.partitionsCount)
            .batchWeighted[ProcessingRecord[OrdersBatch]](
              200000 * precision,
              r => (r.value.totalUSD * precision).toLong,
              _.map(OrdersBatch.empty.add)
            ) { case (batch, single) => single.map(batch.value.add) }
            .runWith(output.sink)
      }
      .run()
  }

}
