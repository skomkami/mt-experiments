package pl.edu.agh.model

import java.time.OffsetDateTime
import io.circe.Encoder
import io.circe.Decoder

case class ProcessedOrder(id: Int,
                          orderDate: OffsetDateTime,
                          buyer: Person,
                          items: List[OrderItem],
                          totalUSD: BigDecimal)

case object ProcessedOrder:
  implicit lazy val decoder: Decoder[ProcessedOrder] = Decoder.derived[ProcessedOrder]
  implicit lazy val encoder: Encoder[ProcessedOrder] = io.circe.generic.auto.deriveEncoder[ProcessedOrder].instance