package pl.edu.agh.cars.generator

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import pl.edu.agh.generator.OrderGenerator
import pl.edu.agh.model.PlainOrder

import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object CsvFileGenerator {
  def orderToString(order: PlainOrder): String = {
    val model = order.model.toString
    val equipment = order.equipment.map(_.toString).mkString(",")
    s"${order.id};${order.date};${order.buyer.name};${order.buyer.address};${order.buyer.email};$model;$equipment"
  }

  val ORDERS_NO = 1000000

  val ordersLines =
    Source
      .fromIterator(() => Iterator.range(1, ORDERS_NO))
      .map(OrderGenerator.randomOrder)
      .wireTap(o => {
        val percentage = o.id.toDouble / ORDERS_NO * 100
        if (percentage.isWhole) println(s"$percentage %")
      })
      .map(orderToString)

  val headerLine =
    "id;date;buyer_name;buyer_address;buyer_email;model;equipment"
  val content = Source.single(headerLine) ++ ordersLines

  def generateContent(implicit mat: Materializer): Future[IOResult] =
    content
      .interleave(Source.repeat("\n"), 1, eagerClose = true)
      .map(ByteString.apply)
      .runWith(FileIO.toPath(Paths.get("orders.csv")))

  def main(args: Array[String]): Unit = {
    OrderGenerator.init
    implicit val system: ActorSystem = ActorSystem("akka-cars-generator")
    implicit val mat: Materializer = Materializer(system)
    implicit val ec: ExecutionContext = mat.executionContext

    println(s"Start: ${System.currentTimeMillis()}ms")
    generateContent.onComplete {
      case Success(value) =>
        println(s"End: ${System.currentTimeMillis()}ms")
        system.terminate()
      case Failure(exception) =>
        throw exception
    }
  }
}
