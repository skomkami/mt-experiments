package pl.edu.agh.akka.pipeline

import akka.actor.ActorSystem
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.circe.generic.decoding.DerivedDecoder
import pl.edu.agh.model.JsonDeserializable
import record.ProcessingRecord

import java.nio.file.Paths

case class FileJsonInput[T: DerivedDecoder](path: String)(
  implicit decoder: JsonDeserializable[T],
  actorSystem: ActorSystem
) extends Input[T] {

  override def source(partitions: Set[Int],
                      partitionCount: Int): Source[ProcessingRecord[T], _] =
//    Source(partitions)
//      .flatMapMerge(
//        partitions.size,
//        p => {
//          val path = Paths.get(s"$path-$p")
    FileIO
      .fromPath(Paths.get(path), 16384)
      .via(
        Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 16384,
          allowTruncation = true
        )
      )
      .map(_.utf8String)
      .map(decoder.unsafeFromJson)
      .map(ProcessingRecord.partitioned[T](_, 0)) //TODO partions
//        }
//      )

}
