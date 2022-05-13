package pl.edu.agh.zio.pipeline

import record.RecordMeta
import zio.kafka.consumer.Offset

case class KafkaRecordMeta(partition: Int, committableOffset: Offset)
    extends RecordMeta
