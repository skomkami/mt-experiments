package pl.edu.agh.zio.pipeline

import io.circe.Decoder
import org.apache.kafka.common.TopicPartition
import pl.edu.agh.util.kafka.KafkaUtil
import zio.{Task, ZIO}
import cats.implicits.toTraverseOps
import pl.edu.agh.model.JsonCodec
import zio.interop.catz.*

abstract class KafkaStatefulPipe[In, S: Decoder] extends StatefulPipe[In, S] {
  override def input: KafkaInput[In]

  override def output: KafkaOutput[S]

  private val consumerName = s"${output.topic}-recover"

  def readMessageAtOffset(tp: TopicPartition, offset: Long): Task[S] = {
    ZIO
      .fromEither(KafkaUtil.getMessageAtOffset(tp, consumerName, offset))
      .map(x => JsonCodec.fromJsonSafe(x))
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
      .attemptBlocking(KafkaUtil.getLastMsgOffset(tp).filterNot(_ < 1))
      .flatMap(_.traverse(readMessageAtOffset(tp, _)))
  }
}
