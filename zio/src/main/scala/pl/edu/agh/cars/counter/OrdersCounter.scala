package pl.edu.agh.cars.counter

import io.circe.generic.decoding.DerivedDecoder
import performancetest.BATCH_ERROR
import performancetest.STOP_AT_ID
import pl.edu.agh.common.Counter
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.zio.pipeline.{FileJsonInput, FileJsonOutput}
import pl.edu.agh.zio.pipeline.StatefulPipe
import zio.Task

case class OrdersCounter() extends StatefulPipe[OrdersBatch, Counter] {

  override def name: String = "zio-orders-counter"

  override def input: FileJsonInput[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    FileJsonInput[OrdersBatch]("zio_order_batch")
  }

  override def output: FileJsonOutput[Counter] = {
    implicit val encoder: JsonSerializable[Counter] = Counter
    FileJsonOutput[Counter]("zio_orders_counter")
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }

  override def restore(partition: Int): Task[Option[Counter]] =
    Task.succeed(None)
}
