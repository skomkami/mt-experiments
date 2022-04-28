package pl.edu.agh.util.kafka

import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.common.TopicPartition

import java.util.Properties
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._
import scala.util.Try

object KafkaUtil {
  def getCommittedOffset(tp: TopicPartition): Option[Long] = {
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
      offsetAndMeta.offset()
    }.fold(fa = msg => {
      scribe.warn(msg.getMessage)
      None
    }, fb = Option.apply)
  }
}
