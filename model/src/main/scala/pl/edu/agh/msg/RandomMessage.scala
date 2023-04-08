package pl.edu.agh.msg

import io.circe.Encoder
import io.circe.Decoder

case class RandomMessage(id: String, x: Double, y: Double)

case object RandomMessage:
  implicit lazy val decoder: Decoder[RandomMessage] = Decoder.derived[RandomMessage]
  implicit lazy val encoder: Encoder[RandomMessage] = io.circe.generic.auto.deriveEncoder[RandomMessage].instance
