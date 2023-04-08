package pl.edu.agh.playground
import zio.{ExitCode, RIO, Scope, Task, URIO, ZIO, ZIOAppArgs, ZIOAppDefault}

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
    ZIO.serviceWithZIO.apply(_.database.lookup(id))

  def update(id: UserID, profile: UserProfile): RIO[Database, Unit] =
    ZIO.serviceWithZIO.apply(_.database.update(id, profile))
}

object Fibers extends ZIOAppDefault {

  val program: ZIO[Any, Nothing, (String, String)] = for {
    fiber1 <- ZIO.succeed("Hi!").fork
    fiber2 <- ZIO.succeed("Bye!").fork
    fiber = fiber1.zip(fiber2)
    tuple <- fiber.join
  } yield tuple

  override def run = program
}
