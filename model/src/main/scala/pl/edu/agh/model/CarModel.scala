package pl.edu.agh.model

import enumeratum._

sealed trait CarModel extends EnumEntry

case object CarModel extends Enum[CarModel] {

  case object Parvus extends CarModel
  case object Mediocriter extends CarModel
  case object Magnum extends CarModel

  override def values: IndexedSeq[CarModel] = findValues
}
