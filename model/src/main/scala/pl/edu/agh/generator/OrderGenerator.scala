package pl.edu.agh.generator

import pl.edu.agh.model.{CarModel, EquipEnum, PlainOrder}

import java.time.OffsetDateTime
import scala.annotation.tailrec
import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

object OrderGenerator {
  def randomOrder(id: Int): PlainOrder = {
    val carModel = Random.nextInt(3).pipe(CarModel.values.apply)
    val equipment = Random.nextInt(3).pipe { noOfEquipment =>
      @tailrec
      def loop(acc: List[EquipEnum], left: Int): List[EquipEnum] = {
        if (left > 0) {
          val leftEnumTypes = EquipEnum.values.toSet.diff(acc.toSet)
          val next =
            Random.nextInt(leftEnumTypes.size).pipe(leftEnumTypes.toList.apply)
          loop(next :: acc, left - 1)
        } else acc
      }

      loop(Nil, noOfEquipment)
    }
    PlainOrder(
      id = id,
      date = OffsetDateTime.now.plusSeconds(id.toLong),
      buyer = PeopleGenerator.randomPerson(),
      model = carModel,
      equipment = equipment
    )
  }

  def init: Unit = OrderGenerator.randomOrder(-1)
}
