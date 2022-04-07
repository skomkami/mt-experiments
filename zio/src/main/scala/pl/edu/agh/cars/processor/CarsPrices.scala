package pl.edu.agh.cars.processor

import pl.edu.agh.model.CarModel
import pl.edu.agh.model.ItemEnum

object CarsPrices {
  case class Price(amount: BigDecimal, currency: String)

  private val parvusPrices: Map[ItemEnum, Price] = Map(
    ItemEnum.CarBase -> Price(10000, "USD"),
    ItemEnum.AirConditioning -> Price(1500, "EUR"),
    ItemEnum.Navigation -> Price(1000, "CHF"),
    ItemEnum.ParkingAssistant -> Price(2000, "GBP"),
  )

  private val mediocriterPrices: Map[ItemEnum, Price] = Map(
    ItemEnum.CarBase -> Price(15000, "USD"),
    ItemEnum.AirConditioning -> Price(2000, "EUR"),
    ItemEnum.Navigation -> Price(1100, "CHF"),
    ItemEnum.ParkingAssistant -> Price(21000, "CNY"),
  )

  private val magnumPrices: Map[ItemEnum, Price] = Map(
    ItemEnum.CarBase -> Price(20000, "USD"),
    ItemEnum.AirConditioning -> Price(2300, "EUR"),
    ItemEnum.Navigation -> Price(1300, "CHF"),
    ItemEnum.ParkingAssistant -> Price(26000, "CNY"),
  )

  val prices: Map[CarModel, Map[ItemEnum, Price]] = Map(
    CarModel.Parvus -> parvusPrices,
    CarModel.Mediocriter -> mediocriterPrices,
    CarModel.Magnum -> magnumPrices,
  )
}
