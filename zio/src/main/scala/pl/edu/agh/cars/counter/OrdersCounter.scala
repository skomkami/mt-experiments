package pl.edu.agh.cars.counter

import io.circe.Decoder
import io.circe.Encoder
import pl.edu.agh.common.Counter
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.zio.pipeline.{KafkaInput, KafkaOutput, KafkaStatefulPipe}

case class OrdersCounter()
    extends KafkaStatefulPipe[OrdersBatch, Counter]() {
  override def name: String = "zio-orders-counter"

  override def input: KafkaInput[OrdersBatch] = {
    KafkaInput[OrdersBatch]("zio_order_batch", name)
  }

  override def output: KafkaOutput[Counter] = {
    KafkaOutput[Counter]("zio_orders_counter")(summon[Encoder[Counter]])
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }
}
