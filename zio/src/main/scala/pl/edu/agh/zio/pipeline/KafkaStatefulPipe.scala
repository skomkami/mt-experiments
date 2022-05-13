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

//  def readMessageAtOffset(tp: TopicPartition, offset: Long): Task[S] = {
//    val layer = (zio.blocking.Blocking.live ++ zio.clock.Clock.live) >>> (consumer ++ zio.console.Console.live)
//
//    val seekOffsetEffect: Task[Unit] = effectBlocking(
//      KafkaUtil.getMessageAtOffset(tp, consumerName, offset - 1)
//    ).provideLayer(zio.blocking.Blocking.live)
//
//    val readMsgEffect: Task[S] = Consumer
//      .subscribeAnd(Subscription.topics(output.topic))
//      .plainStream(Serde.string, messageSerde.asTry)
//      .map { rec =>
//        rec.record.value()
//      }
//      .tap {
//        case Success(value) =>
//          putStr(value.toString)
//        case Failure(exception) =>
//          putStr(exception.getMessage)
//      }
//      .collect { case Success(v) => v }
//      .runHead
//      .flatMap {
//        case Some(value) => ZIO.succeed(value)
//        case None =>
//          ZIO.fail(new Throwable(s"No message at offset: $offset at $tp"))
//      }
//      .provideLayer(layer)
//
//    for {
//      _ <- seekOffsetEffect
//      readMessage <- readMsgEffect
//    } yield {
//      readMessage
//    }
//  }
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
    val tp = new TopicPartition(output.topic, 0)
    ZIO
      .effect(KafkaUtil.getLastMsgOffset(tp).filter(_ < 1))
      .flatMap(_.traverse(readMessageAtOffset(tp, _)))
  }
}
