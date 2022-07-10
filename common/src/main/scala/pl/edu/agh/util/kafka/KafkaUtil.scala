package pl.edu.agh.util.kafka

import org.apache.kafka.clients.admin.{
  Admin,
  AdminClientConfig,
  NewTopic,
  OffsetSpec
}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import java.time.Duration
import java.util
import java.util.Properties
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._
import scala.util.Try

object KafkaUtil {
  private lazy val admin = {
    val properties: Properties = new Properties
    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    Admin.create(properties)
  }

  def getLastMsgOffset(tp: TopicPartition): Option[Long] = {
    Try {
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
      .filter(_ >= 0)
  }

  def createTopics(topics: List[NewTopic]): Unit = {
    val res = admin.createTopics(topics.asJava)
    res
      .values()
      .forEach((x, f) => {
        f.get()
        scribe.info(s"Created topic: $x")
      })
  }

//  def getLastTpsWrittenOffsets(
//    tps: Set[TopicPartition]
//  ): Option[scala.collection.immutable.Set[(Int, Long)]] = {
//    Try {
//      val properties: Properties = new Properties
//      properties.put(
//        AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
//        "localhost:9092"
//      )
//      val admin: Admin = Admin.create(properties)
//      val util: Map[TopicPartition, OffsetSpec] =
//        tps.map(tp => tp -> OffsetSpec.latest()).toMap
//      val offsetsRes =
//        admin.listOffsets(util.asJava)
//      offsetsRes
//        .all()
//        .get(1, TimeUnit.SECONDS)
//        .asScala
//        .map {
//          case (tp, offsetAndMeta) =>
//            tp.partition() -> (offsetAndMeta.offset() - 1)
//        }
//        .toSet
//    }.fold(fa = msg => {
//      scribe.warn(msg)
//      None
//    }, fb = set => Option(set))
//  }

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
