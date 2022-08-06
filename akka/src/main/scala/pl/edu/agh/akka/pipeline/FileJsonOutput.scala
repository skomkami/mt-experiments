package pl.edu.agh.akka.pipeline

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import io.circe.generic.encoding.DerivedAsObjectEncoder
import pl.edu.agh.model.JsonSerializable
import record.ProcessingRecord

import java.nio.file.Paths
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class FileJsonOutput[T: DerivedAsObjectEncoder](path: String)(
  implicit encoder: JsonSerializable[T],
  actorSystem: ActorSystem
) extends Output[T] {

  override def sink: Sink[ProcessingRecord[T], Future[Done]] = {
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    Flow[ProcessingRecord[T]]
      .map(r => encoder.toJson(r.value))
      .map(_.appendedAll("\n"))
      .map(ByteString.apply)
      .to(FileIO.toPath(Paths.get(path)))
      .mapMaterializedValue(_ => Future(Done.done()))
  }
}
