package pl.edu.agh.cars.counter

import akka.actor.ActorSystem
import io.circe.generic.decoding.DerivedDecoder
import performancetest.BATCH_ERROR
import performancetest.STOP_AT_ID
import pl.edu.agh.akka.pipeline.FileJsonInput
import pl.edu.agh.akka.pipeline.FileJsonOutput
import pl.edu.agh.akka.pipeline.StatefulPipe
import pl.edu.agh.common.Counter
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.OrdersBatch

import scala.concurrent.Future

case class OrdersCounter()(implicit as: ActorSystem)
    extends StatefulPipe[OrdersBatch, Counter] {
  override def name: String = "akka-orders-counter"

  override def input: FileJsonInput[OrdersBatch] = {
    implicit val decoder: JsonDeserializable[OrdersBatch] = OrdersBatch
    FileJsonInput[OrdersBatch]("akka_order_batches")
  }

  override def output: FileJsonOutput[Counter] = {
    implicit val encoder: JsonSerializable[Counter] = Counter
    FileJsonOutput[Counter]("akka_orders_counter")
  }

  override def onEvent(oldState: Counter, event: OrdersBatch): Counter =
    oldState.increment(event)

  override def onInit(event: OrdersBatch): Counter = {
    Counter(event.ordersNumber, event.totalAmount)
  }

  override def restore(partition: Int): Future[Option[Counter]] =
    Future.successful(None)
}
