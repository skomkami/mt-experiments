package pl.edu.agh.cars.generator

import cats.effect.{ExitCode, IO, IOApp}
import pl.edu.agh.generator.OrderGenerator
import pl.edu.agh.model.PlainOrder
import fs2._
import fs2.io.file.Files

import java.nio.file.Paths

object CsvFileGenerator extends IOApp {
  def orderToString(order: PlainOrder): String = {
    val model = order.model.toString
    val equipment = order.equipment.map(_.toString).mkString(",")
    s"${order.id};${order.date};${order.buyer.name};${order.buyer.address};${order.buyer.email};$model;$equipment"
  }

  val ORDERS_NO = 1000000

  val ordersLines =
    Stream
      .fromIterator[IO]
      .apply(Iterator.range(1, ORDERS_NO), 200)
      .map(OrderGenerator.randomOrder)
      .evalTap(o => {
        val percentage = o.id.toDouble / ORDERS_NO * 100
        if (percentage.isWhole)
          IO(println(s"$percentage %"))
        else IO.unit
      })
      .map(orderToString)

  val headerLine =
    "id;date;buyer_name;buyer_address;buyer_email;model;equipment"
  val content = Stream(headerLine) ++ ordersLines

  def generateContent: IO[Unit] =
    content
      .interleave(Stream.constant("\n"))
      .flatMap(s => Stream.emits(s.getBytes))
      .through(Files[IO].writeAll(Paths.get("orders.csv")))
      .compile
      .drain

  override def run(args: List[String]): IO[ExitCode] = {
    IO(OrderGenerator.randomOrder(-1))
      .flatTap { _ =>
        println(s"Start: ${System.currentTimeMillis()} ms")
        IO.unit
      }
      .flatMap(_ => generateContent)
      .flatTap { _ =>
        println(s"End: ${System.currentTimeMillis()} ms")
        IO.unit
      }
      .as(ExitCode.Success)
  }
}
