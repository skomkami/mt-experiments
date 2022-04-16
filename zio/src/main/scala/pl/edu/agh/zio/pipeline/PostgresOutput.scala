package pl.edu.agh.zio.pipeline

import pl.edu.agh.cars.persistence.PersistenceService
import pl.edu.agh.cars.persistence.PersistenceService.Persistence
import pl.edu.agh.config.configuration.Configuration
import pl.edu.agh.model.OrdersBatch
import zio.ZIO
import zio.blocking.Blocking
import zio.stream.ZSink

//case class PostgresOutput[T]() extends Output[T] {
//  override def sink: ZSink[Any, _, T, _, _] = ZSink.foreach {
//    (msg: T) =>
//      val persistenceLayer = (Configuration.live ++ Blocking.live) >>> PersistenceService.live
//      val x = ZIO
//        .fromFunctionM[Persistence, Throwable, Int](_.get.save(msg))
//        .provideLayer(persistenceLayer)
//      x
//  }
//}
case class PostgresOutput() extends Output[OrdersBatch] {
  override def sink: ZSink[Any, _, OrdersBatch, _, _] = ZSink.foreach {
    (msg: OrdersBatch) =>
      val persistenceLayer = (Configuration.live ++ Blocking.live) >>> PersistenceService.live
      val outputEffect = ZIO
        .fromFunctionM[Persistence, Throwable, Int](_.get.save(msg))
        .provideLayer(persistenceLayer)
      outputEffect
  }
}
