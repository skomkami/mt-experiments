package pl.edu.agh.msg

import pl.edu.agh.model.JsonCodec

case class RandomMessage(id: String, x: Double, y: Double)

case object RandomMessage extends JsonCodec[RandomMessage]
