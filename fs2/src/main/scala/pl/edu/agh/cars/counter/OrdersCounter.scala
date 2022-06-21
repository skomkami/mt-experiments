package pl.edu.agh.cars.counter

import io.circe.generic.decoding.DerivedDecoder
import performancetest.BATCH_ERROR
import performancetest.STOP_AT_ID
import pl.edu.agh.common.Counter
import pl.edu.agh.fs2.pipeline.KafkaInput
import pl.edu.agh.fs2.pipeline.KafkaOutput
import pl.edu.agh.fs2.pipeline.KafkaStatefulPipe
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch

case class OrdersCounter()
    extends KafkaStatefulPipe[OrdersBatch, Counter]()(
      implicitly[DerivedDecoder[Counter]],
      Counter
    ) {
  override def name: String = "fs2-orders-counter"

  override def input: KafkaInput[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch](
      "fs2_orders_batch",
      name,
      r => r.orders.last.id == STOP_AT_ID - BATCH_ERROR
    )
  }

  override def output: KafkaOutput[Counter] = {
    implicit val encoder: JsonSerializable[Counter] = Counter
    KafkaOutput[Counter]("fs2_orders_counter")
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }
}
