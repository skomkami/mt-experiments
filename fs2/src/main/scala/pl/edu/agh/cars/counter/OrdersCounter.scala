package pl.edu.agh.cars.counter

import pl.edu.agh.common.Counter
import pl.edu.agh.fs2.pipeline.KafkaInput
import pl.edu.agh.fs2.pipeline.KafkaOutput
import pl.edu.agh.fs2.pipeline.KafkaStatefulPipe
import pl.edu.agh.model.OrdersBatch

case class OrdersCounter()
    extends KafkaStatefulPipe[OrdersBatch, Counter]() {
  override def name: String = "fs2-orders-counter"

  override def input: KafkaInput[OrdersBatch] = {
    KafkaInput[OrdersBatch]("fs2_orders_batch", name)
  }

  override def output: KafkaOutput[Counter] = {
    KafkaOutput[Counter]("fs2_orders_counter")
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }
}
