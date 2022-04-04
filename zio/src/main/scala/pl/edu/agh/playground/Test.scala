package pl.edu.agh.playground

import zio.{IO, Ref, ZIO}

// Unsafe State Management
object CountRequests extends zio.App {
  import zio.console._

  def request(counter: Ref[Int]): ZIO[Console, Nothing, Unit] = {
    for {
      _ <- counter.update(_ + 1)
      reqNumber <- counter.get
      _ <- putStrLn(s"request number: $reqNumber").orDie
    } yield ()
  }

  def repeat[E, A](n: Int)(io: IO[E, A]): IO[E, Unit] =
    Ref.make(0).flatMap { iRef =>
      def loop: IO[E, Unit] = iRef.get.flatMap { i =>
        if (i < n)
          io *> iRef.update(_ + 1) *> loop
        else
          IO.unit
      }
      loop
    }

  private val initial = 0
  private val program =
    for {
      ref <- Ref.make(initial)
      _ <- request(ref) zipPar request(ref)
      rn <- ref.get
      _ <- putStrLn(s"total requests performed: $rn").orDie
    } yield ()

  override def run(args: List[String]) = program.exitCode
}
