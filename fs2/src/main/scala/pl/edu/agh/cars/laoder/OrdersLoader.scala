package pl.edu.agh.cars.laoder

import pl.edu.agh.fs2.pipeline.FileJsonOutput
import pl.edu.agh.fs2.pipeline.FileInput
import pl.edu.agh.fs2.pipeline.Input
import pl.edu.agh.fs2.pipeline.Output
import pl.edu.agh.fs2.pipeline.StatelessPipe
import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.PlainOrder
import pl.edu.agh.parser.CsvOrdersParser

case class OrdersLoader(filename: String)
    extends StatelessPipe[String, PlainOrder] {

  override def name: String = "fs2-orders-loader"

  override def onEvent(event: String): PlainOrder = {
    val x = CsvOrdersParser.fromString(event)
    x
  }

  override def input: Input[String] = FileInput(filename)

  override def output: Output[PlainOrder] = {
    implicit val encoder: JsonSerializable[PlainOrder] = PlainOrder
    FileJsonOutput[PlainOrder]("fs2_orders")
  }
}
