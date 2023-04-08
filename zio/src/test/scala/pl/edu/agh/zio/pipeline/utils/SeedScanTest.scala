package pl.edu.agh.zio.pipeline.utils

import zio.stream.ZStream
import pl.edu.agh.zio.pipeline.utils.SeedScan.*
import zio.stream.ZSink
import zio.test.*
object SeedScanTest extends ZIOSpecDefault {

  def spec = suite("SeedScan")(
    test("generate elements only when upstream pushes") {
      val source = ZStream("S", "K", "O", "M", "R", "O")
      val seedScanned = source.seedScan(identity)(_ ++ _)

      val resultZIO = seedScanned.run(ZSink.collectAll[String]).map(_.toList)
      resultZIO.map(res => assertTrue(res == List("S", "SK", "SKO", "SKOM", "SKOMR", "SKOMRO")) )
    }
  )
}
