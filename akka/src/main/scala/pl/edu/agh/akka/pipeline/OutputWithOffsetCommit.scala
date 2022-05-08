package pl.edu.agh.akka.pipeline
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.CommitterSettings
import akka.kafka.scaladsl.Committer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import record.ProcessingRecord

import scala.concurrent.Future

abstract class OutputWithOffsetCommit[T](implicit val as: ActorSystem)
    extends Output[T] {

  private val committerSettings = CommitterSettings(as)

  private val commiterSink: Sink[ProcessingRecord[T], Future[Done]] =
    Flow[ProcessingRecord[T]]
      .collect {
        case ProcessingRecord(_, Some(krm: KafkaRecordMeta)) =>
          krm.committableOffset
      }
      .toMat(Committer.sink(committerSettings))(Keep.right)

  final override def sink: Sink[ProcessingRecord[T], Future[Done]] =
    Flow[ProcessingRecord[T]]
      .alsoTo(elementSink)
      .toMat(commiterSink)(Keep.right)

  def elementSink: Sink[ProcessingRecord[T], Future[Done]]
}
