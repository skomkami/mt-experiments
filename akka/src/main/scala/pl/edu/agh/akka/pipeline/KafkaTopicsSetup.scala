package pl.edu.agh.akka.pipeline

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.admin.NewTopic
import pl.edu.agh.util.kafka.KafkaUtil

import scala.concurrent.Future

object KafkaTopicsSetup {

  def setupKafkaTopicsForPipeline(
    pipeline: Pipeline
  )(implicit mat: Materializer): Future[Done] = {
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
        new NewTopic(topicName, pipeline.config.partitionsCount, 1.toShort)
      }

    Source.single(newTopics).map(KafkaUtil.createTopics).runWith(Sink.ignore)
  }
}
