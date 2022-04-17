package pl.edu.agh.common

trait EntityStore[F[_], T] {
  def save(entity: T): F[Int]
}
