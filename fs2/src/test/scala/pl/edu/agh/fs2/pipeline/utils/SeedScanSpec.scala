package pl.edu.agh.fs2.pipeline.utils

import fs2._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import pl.edu.agh.fs2.pipeline.utils.SeedScan._

class SeedScanSpec extends AnyFlatSpec with should.Matchers {

  "SeedScan" should "generate elements only when upstream pushes" in {
    val source = Stream("S", "K", "O", "M", "R", "O")
    val seedScanned = source.seedScan(identity)(_ ++ _)

    val result = seedScanned.compile.toList

    result shouldEqual List("S", "SK", "SKO", "SKOM", "SKOMR", "SKOMRO")
  }
}
