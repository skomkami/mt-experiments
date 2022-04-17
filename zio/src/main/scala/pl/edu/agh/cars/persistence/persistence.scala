package pl.edu.agh.cars.persistence

import pl.edu.agh.common.EntityStore
import zio.Task

object persistence {
  object Persistence {
    trait Service[T] extends EntityStore[Task, T] {
      def save(ordersBatch: T): Task[Int]
    }
  }
}
