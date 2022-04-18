package pl.edu.agh.cars.batcher

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import pl.edu.agh.akka.pipeline.{Input, KafkaInput, KafkaOutput, Output, Pipe}
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.model.ProcessedOrder

import scala.concurrent.Future

case class OrdersBatcher()(implicit as: ActorSystem)
    extends Pipe[ProcessedOrder, OrdersBatch] {

  override def input: Input[ProcessedOrder] = {
    implicit val decoder: JsonDeserializable[ProcessedOrder] = ProcessedOrder
    KafkaInput[ProcessedOrder]("akka_processed_orders", "akka-orders-batcher")
  }

  override def output: Output[OrdersBatch] = {
    implicit val decoder: JsonSerializable[OrdersBatch] = OrdersBatch
    KafkaOutput[OrdersBatch]("akka_order_batches")
  }
  private val precision = 10

  override def run(implicit mat: Materializer): Future[Done] =
    input.source
      .batchWeighted[OrdersBatch](
        200000 * precision,
        _.totalUSD.precision.toLong,
        OrdersBatch.empty.add
      )(_ add _)
      .runWith(output.sink)

}
