package pl.edu.agh.akka.pipeline

import akka.kafka.ConsumerMessage
import record.RecordMeta

case class KafkaRecordMeta(partition: Int,
                           committableOffset: ConsumerMessage.CommittableOffset)
    extends RecordMeta
