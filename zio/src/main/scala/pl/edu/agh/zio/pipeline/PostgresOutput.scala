package pl.edu.agh.zio.pipeline

import doobie.Transactor
import izumi.reflect.Tag
import pl.edu.agh.cars.persistence.PersistenceService
import pl.edu.agh.cars.persistence.PersistenceService.Persistence
import pl.edu.agh.cars.persistence.persistence.Persistence
import pl.edu.agh.config.configuration.Configuration
import record.ProcessingRecord
import record.RecordMeta
import zio.{Task, ZIO}
import zio.blocking.Blocking
import zio.kafka.consumer.Offset
import zio.stream.ZSink

case class PostgresOutput[T: Tag](
  mkStore: Transactor[Task] => Persistence.Service[T]
) extends OutputWithOffsetCommit[T] {

  private val persistenceLayer = (Configuration.live ++ Blocking.live) >>> PersistenceService
    .live(mkStore)

  override def outputEffect(record: ProcessingRecord[T]): Task[_] = {
    val x = ZIO
      .fromFunctionM[Persistence[T], Throwable, Int](_.get.save(record.value))
      .provideLayer(persistenceLayer)
    x
  }
}
