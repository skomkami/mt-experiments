package pl.edu.agh.cars.loader

import akka.actor.ActorSystem
import pl.edu.agh.akka.pipeline._
import pl.edu.agh.model.{JsonCodec, PlainOrder}
import pl.edu.agh.parser.CsvOrdersParser

case class OrdersLoader(filename: String)(implicit as: ActorSystem)
    extends StatelessPipe[String, PlainOrder] {
  override def name: String = "akka-orders-loader"

  override def input: Input[String] = FileInput(filename)

  override def output: Output[PlainOrder] = {
    implicit val encoder: JsonCodec[PlainOrder] = PlainOrder
    KafkaOutput[PlainOrder]("akka_orders")
  }

  override def onEvent(event: String): PlainOrder =
    CsvOrdersParser.fromString(event)
}
