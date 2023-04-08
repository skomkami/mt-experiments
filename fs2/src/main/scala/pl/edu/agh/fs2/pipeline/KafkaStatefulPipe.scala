package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import cats.implicits.toTraverseOps
import io.circe.{Decoder, Encoder}
import org.apache.kafka.common.TopicPartition
import pl.edu.agh.model.JsonCodec
import pl.edu.agh.util.kafka.KafkaUtil

abstract class KafkaStatefulPipe[In, S: Decoder] extends StatefulPipe[In, S] {
  override def input: KafkaInput[In]

  override def output: KafkaOutput[S]

  private val consumerName = s"${output.topic}-recover"

  private def readMessageAtOffset(tp: TopicPartition, offset: Long): IO[S] = {
    IO.blocking(KafkaUtil.getMessageAtOffset(tp, consumerName, offset))
      .flatMap {
        case Right(value) => IO.pure(value)
        case Left(err)    => IO.raiseError(err)
      }
      .flatMap(str => IO.fromEither(JsonCodec.fromJsonSafe(str)))
  }

  override def restore(partition: Int): IO[Option[S]] = {
    val tp = new TopicPartition(output.topic, partition)
    IO.blocking(KafkaUtil.getLastMsgOffset(tp))
      .map(_.filter(_ == 0))
      .flatMap(_.traverse(readMessageAtOffset(tp, _)))
  }
}
