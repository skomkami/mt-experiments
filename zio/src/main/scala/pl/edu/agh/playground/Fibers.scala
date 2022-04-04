package pl.edu.agh.playground
import zio.{ExitCode, IO, RIO, Task, URIO, ZIO}

class UserID
class UserProfile

object Database {
  trait Service {
    def lookup(id: UserID): Task[UserProfile]
    def update(id: UserID, profile: UserProfile): Task[Unit]
  }
}
trait Database {
  def database: Database.Service
}

object db {
  def lookup(id: UserID): RIO[Database, UserProfile] =
    ZIO.accessM(_.database.lookup(id))

  def update(id: UserID, profile: UserProfile): RIO[Database, Unit] =
    ZIO.accessM(_.database.update(id, profile))
}

object Fibers extends zio.App {

  val program = for {
    fiber1 <- IO.succeed("Hi!").fork
    fiber2 <- IO.succeed("Bye!").fork
    fiber = fiber1.zip(fiber2)
    tuple <- fiber.join
  } yield tuple

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.exitCode
}
