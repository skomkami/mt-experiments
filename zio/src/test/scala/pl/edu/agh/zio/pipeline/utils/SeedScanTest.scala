package pl.edu.agh.zio.pipeline.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import zio.stream.ZStream
import pl.edu.agh.zio.pipeline.utils.SeedScan._
import zio.stream.ZSink

class SeedScanTest extends AnyFlatSpec with should.Matchers {

  "SeedScan" should "generate elements only when upstream pushes" in {
    val source = ZStream("S", "K", "O", "M", "R", "O")
    val seedScanned = source.seedScan(identity)(_ ++ _)

    val resultZIO = seedScanned.run(ZSink.collectAll).map(_.toList)
    val result = zio.Runtime.default.unsafeRun(resultZIO)

    result shouldEqual List("S", "SK", "SKO", "SKOM", "SKOMR", "SKOMRO")
  }
}
