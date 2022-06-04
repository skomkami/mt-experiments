package pl.edu.agh.zio.pipeline

import org.apache.kafka.clients.admin.NewTopic
import pl.edu.agh.config.FlowsConfig
import pl.edu.agh.util.kafka.KafkaUtil
import zio.ZIO

object KafkaTopicsSetup {
  def setupKafkaTopicsForPipeline(
    pipeline: Pipeline
  ): ZIO[FlowsConfig, _, _] = {
    ZIO
      .access[FlowsConfig]
      .apply { flowsConfig =>
        pipeline.pipes
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
            new NewTopic(topicName, flowsConfig.partitionsCount, 1.toShort)
          }
      }
      .flatMap { topics =>
        ZIO.effect(KafkaUtil.createTopics(topics))
      }
  }
}
