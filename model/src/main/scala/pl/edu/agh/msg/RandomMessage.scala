package pl.edu.agh.msg

import pl.edu.agh.model.JsonSerializable

case class RandomMessage(id: String, x: Double, y: Double)

case object RandomMessage extends JsonSerializable[RandomMessage]
