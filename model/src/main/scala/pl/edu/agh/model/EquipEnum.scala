package pl.edu.agh.model

import enumeratum.*

sealed trait EquipEnum extends EnumEntry

object EquipEnum extends Enum[EquipEnum] with CirceEnum[EquipEnum] {

  case object AirConditioning extends EquipEnum
  case object Navigation extends EquipEnum
  case object ParkingAssistant extends EquipEnum

  override def values: IndexedSeq[EquipEnum] = findValues
}
