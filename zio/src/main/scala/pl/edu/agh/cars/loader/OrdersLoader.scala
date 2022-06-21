package pl.edu.agh.cars.loader

import pl.edu.agh.model.{JsonCodec, PlainOrder}
import pl.edu.agh.parser.CsvOrdersParser
import pl.edu.agh.zio.pipeline._

case class OrdersLoader(filename: String)
    extends StatelessPipe[String, PlainOrder] {

  override def name: String = "zio-orders-loader"

  override def input: Input[String] = FileInput(filename)

  override def output: Output[PlainOrder] = {
    implicit val encoder: JsonCodec[PlainOrder] = PlainOrder
    KafkaOutput[PlainOrder]("zio_orders")
  }

  override def onEvent(event: String): PlainOrder =
    CsvOrdersParser.fromString(event)
}
