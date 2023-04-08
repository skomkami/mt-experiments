package pl.edu.agh.model

import io.circe.{ Decoder, Encoder }
case class OrdersBatch(ordersNumber: Int,
                       orders: List[ProcessedOrder],
                       totalAmount: BigDecimal):

  def add(order: ProcessedOrder): OrdersBatch =
    OrdersBatch(ordersNumber + 1, order :: orders, totalAmount + order.totalUSD)

case object OrdersBatch:
  implicit lazy val decoder: Decoder[OrdersBatch] = Decoder.derived[OrdersBatch]
  implicit lazy val encoder: Encoder[OrdersBatch] = io.circe.generic.auto.deriveEncoder[OrdersBatch].instance
  def empty: OrdersBatch = OrdersBatch(0, Nil, 0)
