package pl.edu.agh.fs2.pipeline.utils

import org.scalatest._
import flatspec._
import matchers._
import fs2._
import GroupUntil._

class GroupUntilSpec extends AnyFlatSpec with should.Matchers {
  "GroupUntil" should "batch elements until condition is met" in {
    val parcels = Stream(
      Parcel(3),
      Parcel(2),
      Parcel(1),
      Parcel(3),
      Parcel(2),
      Parcel(5),
      Parcel(6),
      Parcel(3),
      Parcel(8)
    )

    val batched = parcels.groupUntil(Container(10))(_ canContain _)(_ plus _)

    val list: List[Container] = batched.compile.toList

    list.size shouldEqual 4
    list.head.parcels.size shouldEqual 4
    list(1).currentWeight shouldEqual 7
  }
}

case class Parcel(weight: Int)
case class Container(maxWeight: Int,
                     currentWeight: Int = 0,
                     parcels: List[Parcel] = Nil) {
  def canContain(parcel: Parcel): Boolean =
    currentWeight + parcel.weight < maxWeight

  def plus(parcel: Parcel): Container = {
    copy(
      currentWeight = currentWeight + parcel.weight,
      parcels = parcel :: parcels
    )
  }
}
