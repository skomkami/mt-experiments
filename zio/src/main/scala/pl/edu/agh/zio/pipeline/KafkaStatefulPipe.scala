package pl.edu.agh.zio.pipeline

import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.common.TopicPartition
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.util.kafka.KafkaUtil
import zio.{Task, ZIO}
import cats.implicits.toTraverseOps
import zio.interop.catz._

abstract class KafkaStatefulPipe[In, S: DerivedDecoder](
  implicit val decoder: JsonDeserializable[S]
) extends StatefulPipe[In, S] {
  override def input: KafkaInput[In]

  override def output: KafkaOutput[S]

  private val consumerName = s"${output.topic}-recover"

  def readMessageAtOffset(tp: TopicPartition, offset: Long): Task[S] = {
    ZIO
      .fromEither(KafkaUtil.getMessageAtOffset(tp, consumerName, offset))
      .map(decoder.fromJson)
      .flatMap {
        case Right(value) => ZIO.succeed(value)
        case Left(err) =>
          ZIO.fail(
            new Throwable(s"No message at offset: $offset at $tp. Reason: $err")
          )
      }
  }

  override def restore(partition: Int): Task[Option[S]] = {
    val tp = new TopicPartition(output.topic, partition)
    ZIO
      .effect(KafkaUtil.getLastMsgOffset(tp).filterNot(_ < 1))
      .flatMap(_.traverse(readMessageAtOffset(tp, _)))
  }
}
