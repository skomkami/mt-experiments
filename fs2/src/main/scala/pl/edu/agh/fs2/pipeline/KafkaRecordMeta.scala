package pl.edu.agh.fs2.pipeline

import cats.effect.IO
import fs2.kafka.CommittableOffset
import record.RecordMeta

case class KafkaRecordMeta(partition: Int,
                           committableOffset: CommittableOffset[IO])
    extends RecordMeta
