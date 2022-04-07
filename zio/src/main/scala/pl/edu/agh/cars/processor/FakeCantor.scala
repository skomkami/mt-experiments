package pl.edu.agh.cars.processor

import scala.util.Random

object FakeCantor {
  private val exchange: Map[String, BigDecimal] =
    Map("EUR" -> 1.09, "CHF" -> 1.07, "GBP" -> 1.31, "CNY" -> 0.16)

  private def randomFactor = (Random.nextInt(10000).toDouble - 5000) / 5000

  def getUSDFor(currency: String, amount: BigDecimal): BigDecimal =
    currency match {
      case "USD"                         => amount
      case "EUR" | "CHF" | "GBP" | "CNY" => exchange(currency) * randomFactor
      case _                             => throw new Exception("Unsupported currency")
    }
}
