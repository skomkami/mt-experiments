package pl.edu.agh.fs2.pipeline

import cats.effect.Async
import cats.effect.IO
import fs2.Stream
import fs2.kafka.AdminClientSettings
import fs2.kafka.KafkaAdminClient
import org.apache.kafka.clients.admin.NewTopic

object KafkaTopicsSetup {

  def kafkaAdminClientStream[F[_]: Async](
    bootstrapServers: String
  ): Stream[F, KafkaAdminClient[F]] =
    KafkaAdminClient.stream[F](AdminClientSettings(bootstrapServers))

  def setupKafkaTopicsForPipeline(pipeline: Pipeline): IO[Unit] = {
    val newTopics = pipeline.pipes
      .flatMap { pipe =>
        val maybeInputTopic = pipe.input match {
          case kt: KafkaInput[_] => Some(kt.topic)
          case _                 => None
        }
        val maybeOutputTopic = pipe.output match {
          case kt: KafkaOutput[_] => Some(kt.topic)
          case _                  => None
        }
        maybeInputTopic.toList ++ maybeOutputTopic
      }
      .distinct
      .map { topicName =>
        new NewTopic(topicName, pipeline.flowsConfig.partitionsCount, 1.toShort)
      }

    kafkaAdminClientStream[IO]("localhost:9092")
      .mapAsync(1)(_.createTopics(newTopics))
      .compile
      .drain
  }
}
