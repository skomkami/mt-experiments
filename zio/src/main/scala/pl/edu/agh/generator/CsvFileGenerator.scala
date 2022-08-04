package pl.edu.agh.generator
import pl.edu.agh.model.PlainOrder
import zio._
import zio.blocking.Blocking
import zio.console.{Console, putStrLn}
import zio.stream._

import java.nio.file.Paths

object CsvFileGenerator extends zio.App {
  def orderToString(order: PlainOrder): String = {
    val model = order.model.toString
    val equipment = order.equipment.map(_.toString).mkString(",")
    s"${order.id};${order.date};${order.buyer.name};${order.buyer.address};${order.buyer.email};$model;$equipment"
  }

  val ORDERS_NO = 1000000

  val ordersLines =
    ZStream
      .fromIterator(Iterator.range(1, ORDERS_NO))
      .map(OrderGenerator.randomOrder)
      .tap(o => {
        val percentage = (o.id.toDouble / ORDERS_NO * 100)
        if (percentage.isWhole)
          console.putStr(s"$percentage %")
        else ZIO.none
      })
      .map(orderToString)

  val headerLine =
    "id;date;buyer_name;buyer_address;buyer_email;model;equipment"
  val content = ordersLines

  def generateContent: ZIO[Blocking with Console, Throwable, Long] =
    content
      .interleave(ZStream.repeat("\n"))
      .mapConcat(_.getBytes)
      .run(ZSink.fromFile(Paths.get("orders.csv")))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    ZIO
      .succeed(OrderGenerator.randomOrder(-1))
      .map(_ => ZIO.succeed(println(s"Start: ${System.currentTimeMillis()}ms")))
      .flatMap(_ => generateContent)
      .map(_ => ZIO.succeed(println(s"End: ${System.currentTimeMillis()}ms")))
      .exitCode
  }
}
