package pl.edu.agh.akka.pipeline.util

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import pl.edu.agh.akka.pipeline.util.SeedScan.SeedScanOps

import scala.concurrent.Await
import scala.concurrent.duration._

class SeedScanSuite extends AnyFlatSpec with should.Matchers {
  implicit val system = ActorSystem("seed-scan-suite")

  "SeedScan" should "generate elements only when upstream pushes" in {
    val source = Source(List("S", "K", "O", "M", "R", "O"))
    val seedScanned = source.seedScan(identity)(_ ++ _)

    val result = Await.result(seedScanned.runWith(Sink.seq), 1.second)
    result shouldEqual Seq("S", "SK", "SKO", "SKOM", "SKOMR", "SKOMRO")
  }
}
