package pl.edu.agh.zio.pipeline

import doobie.Transactor
import izumi.reflect.Tag
import pl.edu.agh.cars.persistence.PersistenceService
import pl.edu.agh.cars.persistence.PersistenceService.Persistence
import pl.edu.agh.cars.persistence.persistence.Persistence
import pl.edu.agh.config.configuration.Configuration
import zio.{Task, ZIO}
import zio.blocking.Blocking
import zio.stream.ZSink

case class PostgresOutput[T: Tag](
  mkStore: Transactor[Task] => Persistence.Service[T]
) extends Output[T] {
  override def sink: ZSink[Any, _, T, _, _] = ZSink.foreach { (msg: T) =>
    val persistenceLayer = (Configuration.live ++ Blocking.live) >>> PersistenceService
      .live(mkStore)
    val x = ZIO
      .fromFunctionM[Persistence[T], Throwable, Int](_.get.save(msg))
      .provideLayer(persistenceLayer)
    x
  }
}
//case class PostgresOutput() extends Output[OrdersBatch] {
//  override def sink: ZSink[Any, _, OrdersBatch, _, _] = ZSink.foreach {
//    (msg: OrdersBatch) =>
//      val persistenceLayer = (Configuration.live ++ Blocking.live) >>> PersistenceService.live
//      val outputEffect = ZIO
//        .fromFunctionM[Persistence, Throwable, Int](_.get.save(msg))
//        .provideLayer(persistenceLayer)
//      outputEffect
//  }
//}
