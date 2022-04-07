package pl.edu.agh.model

import enumeratum.CirceEnum
import enumeratum.Enum
import enumeratum.EnumEntry

sealed trait ItemEnum extends EnumEntry

object ItemEnum extends Enum[ItemEnum] with CirceEnum[ItemEnum] {

  case object CarBase extends ItemEnum
  case object AirConditioning extends ItemEnum
  case object Navigation extends ItemEnum
  case object ParkingAssistant extends ItemEnum

  override def values: IndexedSeq[ItemEnum] = findValues
}
