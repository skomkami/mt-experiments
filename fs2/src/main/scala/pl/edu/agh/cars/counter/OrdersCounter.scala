package pl.edu.agh.cars.counter

import cats.effect.IO
import pl.edu.agh.common.Counter
import pl.edu.agh.fs2.pipeline.FileJsonInput
import pl.edu.agh.fs2.pipeline.FileJsonOutput
import pl.edu.agh.fs2.pipeline.StatefulPipe
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch

case class OrdersCounter() extends StatefulPipe[OrdersBatch, Counter] {
  override def name: String = "fs2-orders-counter"

  override def input: FileJsonInput[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    FileJsonInput[OrdersBatch]("fs2_orders_batch")
  }

  override def output: FileJsonOutput[Counter] = {
    implicit val encoder: JsonSerializable[Counter] = Counter
    FileJsonOutput[Counter]("fs2_orders_counter")
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }

  override def restore(partition: Int): IO[Option[Counter]] = IO.none
}
