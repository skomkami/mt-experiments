package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import cats.implicits.toTraverseOps
import fs2.kafka._
import io.circe.generic.decoding.DerivedDecoder
import org.apache.kafka.common.TopicPartition
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import pl.edu.agh.model.JsonDeserializable
import pl.edu.agh.util.kafka.KafkaUtil

abstract class KafkaStatefulPipe[In, S: DerivedDecoder](
  implicit val decoder: JsonDeserializable[S]
) extends StatefulPipe[In, S] {
  override def input: KafkaInput[In]

  override def output: KafkaOutput[S]

  val messageDeserializer = Deserializer.instance { (_, _, bytes) =>
    val either = decoder.fromJson(new String(bytes))
    IO.fromEither(either)
  }
  val consumerSettings =
    ConsumerSettings[IO, String, S](
      keyDeserializer = Deserializer[IO, String],
      valueDeserializer = messageDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("fs2-recover")
      .withCloseTimeout(1.minute)

  private def readMessageAtOffset(offset: Long): IO[S] = {
    KafkaConsumer
      .stream(consumerSettings)
      .evalTap(_.seek(new TopicPartition(output.topic, 0), offset - 1))
      .subscribeTo(output.topic)
      .records
      .take(1)
      .compile
      .toList
      .map(_.head)
      .map(_.record.value)

  }

  override def restore: IO[Option[S]] = {
    IO(KafkaUtil.getCommittedOffset(new TopicPartition(output.topic, 0)))
      .flatMap(_.traverse(readMessageAtOffset))
  }
}
