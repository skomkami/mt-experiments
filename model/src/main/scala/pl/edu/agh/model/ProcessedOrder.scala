package pl.edu.agh.model

import java.time.OffsetDateTime

case class ProcessedOrder(id: Int,
                          orderDate: OffsetDateTime,
                          buyer: Person,
                          items: List[OrderItem],
                          totalUSD: BigDecimal)

case object ProcessedOrder extends JsonCodec[ProcessedOrder]
