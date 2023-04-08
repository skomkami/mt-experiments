package pl.edu.agh.model

import io.circe.*
case class OrderItem(model: CarModel,
                     `type`: ItemEnum,
                     price: BigDecimal,
                     currency: String,
                     priceUSD: BigDecimal)

case object OrderItem:
  implicit lazy val decoder: Decoder[OrderItem] = Decoder.derived[OrderItem]
  implicit lazy val encoder: Encoder[OrderItem] = io.circe.generic.auto.deriveEncoder[OrderItem].instance