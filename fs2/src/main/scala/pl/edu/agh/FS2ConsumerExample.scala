package pl.edu.agh

import cats.effect._
import fs2.kafka._
import pl.edu.agh.msg.RandomMessage

import scala.concurrent.duration._

object FS2ConsumerExample extends IOApp {
  val messageDeserializer = Deserializer.instance { (topic, headers, bytes) =>
    val either = RandomMessage.fromJson(new String(bytes))
    IO.fromEither(either)
  }
  val consumerSettings =
    ConsumerSettings[IO, String, RandomMessage](
      keyDeserializer = Deserializer[IO, String],
      valueDeserializer = messageDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("fs2-consumer")
      .withCloseTimeout(1.minute)

  def processRecord(record: ConsumerRecord[String, RandomMessage]): IO[Unit] =
    IO(println(s"Processing record: $record"))

  override def run(args: List[String]): IO[ExitCode] = {
    val stream =
      KafkaConsumer
        .stream(consumerSettings)
        .subscribeTo("messages")
        .records
        .mapAsync(1)(cr => processRecord(cr.record))

    stream.compile.drain.as(ExitCode.Success)
  }
}
