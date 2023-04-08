package pl.edu.agh.model

import enumeratum.*

sealed trait CarModel extends EnumEntry

case object CarModel extends Enum[CarModel] with CirceEnum[CarModel] {

  case object Parvus extends CarModel
  case object Mediocriter extends CarModel
  case object Magnum extends CarModel

  override def values: IndexedSeq[CarModel] = findValues
}
