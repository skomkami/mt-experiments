package pl.edu.agh.cars.processor

import pl.edu.agh.common.{CarsPrices, FakeCantor}
import pl.edu.agh.model.EquipEnum
import pl.edu.agh.model.ItemEnum
import pl.edu.agh.model.JsonCodec
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.OrderItem
import pl.edu.agh.model.PlainOrder
import pl.edu.agh.model.ProcessedOrder
import pl.edu.agh.zio.pipeline.Input
import pl.edu.agh.zio.pipeline.KafkaInput
import pl.edu.agh.zio.pipeline.KafkaOutput
import pl.edu.agh.zio.pipeline.Output
import pl.edu.agh.zio.pipeline.StatelessPipe

case class OrdersProcessor() extends StatelessPipe[PlainOrder, ProcessedOrder] {
  override def onEvent(plainOrder: PlainOrder): ProcessedOrder = {
    val itemTypes: List[ItemEnum] = ItemEnum.CarBase :: plainOrder.equipment
      .map {
        case EquipEnum.AirConditioning  => ItemEnum.AirConditioning
        case EquipEnum.Navigation       => ItemEnum.Navigation
        case EquipEnum.ParkingAssistant => ItemEnum.ParkingAssistant
      }

    val orderItems: List[OrderItem] = itemTypes.map { item =>
      val price = CarsPrices.prices(plainOrder.model)(item)
      OrderItem(
        model = plainOrder.model,
        `type` = item,
        price = price.amount,
        currency = price.currency,
        priceUSD = FakeCantor.getUSDFor(price.currency, price.amount)
      )
    }
    ProcessedOrder(
      id = plainOrder.id,
      orderDate = plainOrder.date,
      buyer = plainOrder.buyer,
      items = orderItems,
      totalUSD = orderItems.map(_.priceUSD).sum
    )
  }

  override def input: Input[PlainOrder] = {
    implicit val decoder: JsonDeserializable[PlainOrder] = PlainOrder
    KafkaInput[PlainOrder]("zio_orders", "orders-processor")
  }

  override def output: Output[ProcessedOrder] = {
    implicit val encoder: JsonCodec[ProcessedOrder] = ProcessedOrder
    KafkaOutput[ProcessedOrder]("zio_processed_orders")
  }
}
