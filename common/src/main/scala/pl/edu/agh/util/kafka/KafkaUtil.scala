package pl.edu.agh.util.kafka

import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import java.time.Duration
import java.util
import java.util.Properties
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object KafkaUtil {
  def getLastMsgOffset(tp: TopicPartition): Option[Long] = {
    Try {
      val properties: Properties = new Properties
      properties.put(
        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
        "localhost:9092"
      )
      val admin: Admin = Admin.create(properties)
      val util: Map[TopicPartition, OffsetSpec] = Map(tp -> OffsetSpec.latest())
      val offsetsRes =
        admin.listOffsets(util.asJava)
      val offsetAndMeta = offsetsRes
        .partitionResult(tp)
        .get(1, TimeUnit.SECONDS)
      offsetAndMeta.offset() - 1
    }.fold(fa = msg => {
      scribe.warn(msg)
      None
    }, fb = Option.apply)
  }

  def getMessageAtOffset(tp: TopicPartition,
                         consumerName: String,
                         offset: Long): Either[Throwable, String] = {
    val props: Properties = new Properties()
    props.setProperty("bootstrap.servers", "localhost:9092")
    props.setProperty("group.id", consumerName)
    props.setProperty("enable.auto.commit", "true")
    props.setProperty("auto.offset.reset", "earliest")
    props.setProperty("max.poll.records", "1")
    props.setProperty(
      "key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer"
    )
    props.setProperty(
      "value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer"
    )
    val consumer: KafkaConsumer[String, String] =
      new KafkaConsumer[String, String](props)

    Try {
      consumer.assign(
        new util.ArrayList[TopicPartition](util.Arrays.asList(tp))
      )
      consumer.seek(tp, offset)
      val records: ConsumerRecords[String, String] =
        consumer.poll(Duration.ofSeconds(5))
      val firstRecord = records.iterator().next()
      firstRecord.value()
    }.fold(
      err => Left(err),
//       Left(s"Could not read message at $tp offset: $offset. Reason: $err"),
      Right.apply
    )
  }

}
