package pl.edu.agh.model

case class OrdersBatch(ordersNumber: Int,
                       orders: List[ProcessedOrder],
                       totalAmount: BigDecimal) {

  def add(order: ProcessedOrder): OrdersBatch = {
    OrdersBatch(ordersNumber + 1, order :: orders, totalAmount + order.totalUSD)
  }
}

case object OrdersBatch extends JsonCodec[OrdersBatch] {
  def empty: OrdersBatch = OrdersBatch(0, Nil, 0)
}
