//package pl.edu.agh.cars.batcher
//
//import cats.effect.IO
//import pl.edu.agh.fs2.pipeline.{Input, KafkaInput, KafkaOutput, Output, Pipe}
//import pl.edu.agh.model.{
//  JsonDeserializable,
//  JsonSerializable,
//  OrdersBatch,
//  ProcessedOrder
//}
//
//class OrdersBatcher() extends Pipe[ProcessedOrder, OrdersBatch] {
//
//  override def input: Input[ProcessedOrder] = {
//    implicit val decoder: JsonDeserializable[ProcessedOrder] = ProcessedOrder
//    KafkaInput[ProcessedOrder]("fs2_processed_orders", "fs2-orders-batcher")
//  }
//
//  override def output: Output[OrdersBatch] = {
//    implicit val decoder: JsonSerializable[OrdersBatch] = OrdersBatch
//    KafkaOutput[OrdersBatch]("fs2_orders_batch")
//  }
//
//  override def run: IO[_] =
//    input.source
//      .fold()
//      .run(output.sink)
//
//}