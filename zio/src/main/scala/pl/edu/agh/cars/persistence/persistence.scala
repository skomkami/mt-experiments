package pl.edu.agh.cars.persistence

import pl.edu.agh.model.OrdersBatch
import zio.Task

object persistence {
  object Persistence {
    trait Service {
      def saveBatch(ordersBatch: OrdersBatch): Task[Int]
    }
  }
}
