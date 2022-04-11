package pl.edu.agh.cars.laoder

import pl.edu.agh.fs2.pipeline.{
  FileInput,
  Input,
  KafkaOutput,
  Output,
  StatelessPipe
}
import pl.edu.agh.model.{JsonSerializable, PlainOrder}
import pl.edu.agh.parser.CsvOrdersParser

case class OrdersLoader(filename: String)
    extends StatelessPipe[String, PlainOrder] {
  override def onEvent(event: String): PlainOrder =
    CsvOrdersParser.fromString(event)

  override def input: Input[String] = FileInput(filename)

  override def output: Output[PlainOrder] = {
    implicit val encoder: JsonSerializable[PlainOrder] = PlainOrder
    KafkaOutput[PlainOrder]("fs2_orders")
  }
}
