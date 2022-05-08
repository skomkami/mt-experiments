package record

trait RecordMeta {
  def partition: Int
}

case class BasicRecordMeta(partition: Int) extends RecordMeta

case class ProcessingRecord[T](value: T, meta: Option[RecordMeta] = None) {
  def map[T2](f: T => T2): ProcessingRecord[T2] = copy(value = f(value))
}

case object ProcessingRecord {
  def partitioned[T](value: T, partition: Int): ProcessingRecord[T] = {
    ProcessingRecord(value, Some(BasicRecordMeta(partition)))
  }
}
