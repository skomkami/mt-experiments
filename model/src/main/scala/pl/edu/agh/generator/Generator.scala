package pl.edu.agh.generator

import pl.edu.agh.msg.RandomMessage

import java.util.UUID

case object Generator {
  def generateMsg(): RandomMessage = {
    RandomMessage(
      id = UUID.randomUUID().toString,
      x = scala.util.Random.nextDouble(),
      y = scala.util.Random.nextDouble()
    )
  }
}
