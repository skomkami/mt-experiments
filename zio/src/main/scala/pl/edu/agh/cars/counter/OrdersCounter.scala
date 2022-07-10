package pl.edu.agh.cars.counter

import io.circe.generic.decoding.DerivedDecoder
import performancetest.BATCH_ERROR
import performancetest.STOP_AT_ID
import pl.edu.agh.common.Counter
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.zio.pipeline.{KafkaInput, KafkaOutput, KafkaStatefulPipe}

case class OrdersCounter()
    extends KafkaStatefulPipe[OrdersBatch, Counter]()(
      implicitly[DerivedDecoder[Counter]],
      Counter
    ) {

  override def name: String = "zio-orders-counter"

  override def input: KafkaInput[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch](
      "zio_order_batch",
      name,
      r => r.orders.exists(_.id >= STOP_AT_ID - BATCH_ERROR - 12)
    )
  }

  override def output: KafkaOutput[Counter] = {
    implicit val encoder: JsonSerializable[Counter] = Counter
    KafkaOutput[Counter]("zio_orders_counter")
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }
}
