package pl.edu.agh.common

import io.circe.{Decoder, Encoder}
import io.circe.`export`.Exported
import pl.edu.agh.model.OrdersBatch
import pl.edu.agh.msg.RandomMessage
//import io.circe.Encoder.AsObject.importedAsObjectEncoder
//import io.circe.Encoder.AsRoot.importedAsRootEncoder
//import io.circe.Encoder.importedEncoder

case class Counter(ordersNo: Long, totalAmount: BigDecimal):
  def increment(batch: OrdersBatch): Counter =
    copy(
      ordersNo + batch.ordersNumber,
      totalAmount = totalAmount + batch.totalAmount
    )

case object Counter:
  implicit lazy val decoder: Decoder[Counter] = Decoder.derived[Counter]
  implicit lazy val exportedEncoder: Exported[Encoder[Counter]] = io.circe.generic.auto.deriveEncoder[Counter]
  implicit lazy val encoder: Encoder[Counter] = exportedEncoder.instance

  def empty: Counter = Counter(0, 0)

