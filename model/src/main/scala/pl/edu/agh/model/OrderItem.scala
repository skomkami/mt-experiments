package pl.edu.agh.model

case class OrderItem(model: CarModel,
                     `type`: ItemEnum,
                     price: BigDecimal,
                     currency: String,
                     priceUSD: BigDecimal)

case object OrderItem extends JsonCodec[OrderItem]
