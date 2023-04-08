package pl.edu.agh.model

import java.time.OffsetDateTime
import io.circe.Encoder
import io.circe.Decoder

case class PlainOrder(id: Int,
                      date: OffsetDateTime,
                      buyer: Person,
                      model: CarModel,
                      equipment: List[EquipEnum])

case object PlainOrder:
  implicit lazy val decoder: Decoder[PlainOrder] = Decoder.derived[PlainOrder]
  implicit lazy val encoder: Encoder[PlainOrder] = io.circe.generic.auto.deriveEncoder[PlainOrder].instance