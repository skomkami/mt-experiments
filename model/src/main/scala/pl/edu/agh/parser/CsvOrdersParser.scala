package pl.edu.agh.parser

import pl.edu.agh.model.{CarModel, EquipEnum, Person, PlainOrder}

import java.time.OffsetDateTime

object CsvOrdersParser:
  def fromString(orderStr: String): PlainOrder =
    val columns = orderStr.split(";")
    PlainOrder(
      id = columns(0).toInt,
      date = OffsetDateTime.parse(columns(1)),
      buyer =
        Person(name = columns(2), address = columns(3), email = columns(4)),
      model = CarModel.withName(columns(5)),
      equipment =
        columns.lift(6).toList.flatMap(_.split(",")).map(EquipEnum.withName)
    )
