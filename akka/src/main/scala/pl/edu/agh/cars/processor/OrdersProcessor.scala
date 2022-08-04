package pl.edu.agh.cars.processor

import akka.actor.ActorSystem
import pl.edu.agh.akka.pipeline.{
  Input,
  KafkaInput,
  KafkaOutput,
  Output,
  StatelessPipe
}
import pl.edu.agh.common.{CarsPrices, FakeCantor}
import pl.edu.agh.model.EquipEnum
import pl.edu.agh.model.ItemEnum
import pl.edu.agh.model.JsonCodec
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.model.OrderItem
import pl.edu.agh.model.PlainOrder
import pl.edu.agh.model.ProcessedOrder

case class OrdersProcessor()(implicit as: ActorSystem)
    extends StatelessPipe[PlainOrder, ProcessedOrder] {
  override def name: String = "akka-orders-processor"

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
    KafkaInput[PlainOrder]("akka_orders", name)
  }

  override def output: Output[ProcessedOrder] = {
    implicit val encoder: JsonCodec[ProcessedOrder] = ProcessedOrder
    KafkaOutput[ProcessedOrder]("akka_processed_orders")
  }
}
