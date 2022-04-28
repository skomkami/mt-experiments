package pl.edu.agh.common

import pl.edu.agh.model.JsonCodec
import pl.edu.agh.model.OrdersBatch

case class Counter(ordersNo: Long, totalAmount: BigDecimal) {
  def increment(batch: OrdersBatch): Counter = {
    copy(
      ordersNo + batch.ordersNumber,
      totalAmount = totalAmount + batch.totalAmount
    )
  }
}

object Counter extends JsonCodec[Counter] {
  def empty: Counter = Counter(0, 0)
}
