package pl.edu.agh.cars.counter

import akka.actor.ActorSystem
import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.akka.pipeline.{KafkaInput, KafkaOutput, KafkaStatefulPipe}
import pl.edu.agh.model.{JsonDeserializable, JsonSerializable, OrdersBatch}

case class OrdersCounter()(implicit as: ActorSystem)
    extends KafkaStatefulPipe[OrdersBatch, Counter]()(
      implicitly[DerivedDecoder[Counter]],
      Counter,
      as
    ) {
  override def input: KafkaInput[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    KafkaInput[OrdersBatch]("akka_order_batches", "akka-orders-counter")
  }

  override def output: KafkaOutput[Counter] = {
    implicit val encoder: JsonSerializable[Counter] = Counter
    KafkaOutput[Counter]("akka_orders_counter")
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }
}
