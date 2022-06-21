package pl.edu.agh.cars.processor

import performancetest.STOP_AT_ID
import pl.edu.agh.common.{CarsPrices, FakeCantor}
import pl.edu.agh.fs2.pipeline.{
  Input,
  KafkaInput,
  KafkaOutput,
  Output,
  StatelessPipe
}
import pl.edu.agh.model.{
  EquipEnum,
  ItemEnum,
  JsonCodec,
  JsonDeserializable,
  OrderItem,
  PlainOrder,
  ProcessedOrder
}

case class OrdersProcessor() extends StatelessPipe[PlainOrder, ProcessedOrder] {

  override def name: String = "fs2-orders-processor"

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
    KafkaInput[PlainOrder]("fs2_orders", name, r => r.id == STOP_AT_ID)
  }

  override def output: Output[ProcessedOrder] = {
    implicit val encoder: JsonCodec[ProcessedOrder] = ProcessedOrder
    KafkaOutput[ProcessedOrder]("fs2_processed_orders")
  }
}
