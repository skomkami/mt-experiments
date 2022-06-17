package pl.edu.agh.generator
import pl.edu.agh.model.PlainOrder
import zio._
import zio.stream._

import java.nio.file.Paths

object CsvFileGenerator extends zio.App {
  def orderToString(order: PlainOrder): String = {
    val model = order.model.toString
    val equipment = order.equipment.map(_.toString).mkString(",")
    s"${order.id};${order.date};${order.buyer.name};${order.buyer.address};${order.buyer.email};$model;$equipment"
  }

  val ordersLines =
    ZStream
      .fromIterator(Iterator.range(1, 1000000))
      .map(OrderGenerator.randomOrder)
      .map(orderToString)

  val headerLine =
    "id;date;buyer_name;buyer_address;buyer_email;model;equipment"
  val content = ZStream.succeed(headerLine) ++ ordersLines

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    content
      .interleave(ZStream.repeat("\n"))
      .tap(console.putStr(_))
      .mapConcat(_.getBytes)
      .run(ZSink.fromFile(Paths.get("orders.csv")))
      .exitCode
}
