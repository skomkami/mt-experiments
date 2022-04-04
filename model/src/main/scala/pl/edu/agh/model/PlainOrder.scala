package pl.edu.agh.model

import java.time.OffsetDateTime

case class PlainOrder(id: Int,
                      date: OffsetDateTime,
                      buyer: Person,
                      model: CarModel,
                      equipment: List[EquipEnum])

case object PlainOrder extends JsonSerializable[PlainOrder]
