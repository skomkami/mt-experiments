package pl.edu.agh.cars.loader

//import pl.edu.agh.model.JsonSerializable
import pl.edu.agh.model.CarModel
import pl.edu.agh.model.EquipEnum
import pl.edu.agh.model.JsonCodec
import pl.edu.agh.model.Person
import pl.edu.agh.model.PlainOrder
import pl.edu.agh.zio.pipeline.FileInput
import pl.edu.agh.zio.pipeline.Input
import pl.edu.agh.zio.pipeline.KafkaOutput
import pl.edu.agh.zio.pipeline.Output
import pl.edu.agh.zio.pipeline.StatelessPipe

import java.time.OffsetDateTime

case class OrdersLoader(filename: String)
    extends StatelessPipe[String, PlainOrder] {
  override def input: Input[String] = FileInput(filename)

  override def output: Output[PlainOrder] = {
    implicit val encoder: JsonCodec[PlainOrder] = PlainOrder
    KafkaOutput[PlainOrder]("zio-orders")
  }

  override def onEvent(event: String): PlainOrder = {
    val columns = event.split(";")
    PlainOrder(
      id = columns(0).toInt,
      date = OffsetDateTime.parse(columns(1)),
      buyer =
        Person(name = columns(2), address = columns(3), email = columns(4)),
      model = CarModel.withName(columns(5)),
      equipment =
        columns.lift(6).toList.flatMap(_.split(",")).map(EquipEnum.withName)
    )
  }
}
