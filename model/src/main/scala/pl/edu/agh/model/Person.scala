package pl.edu.agh.model

import io.circe.Encoder
import io.circe.Decoder

case class Person(name: String, address: String, email: String)

case object Person:
  implicit lazy val decoder: Decoder[Person] = Decoder.derived[Person]
  implicit lazy val encoder: Encoder[Person] = io.circe.generic.auto.deriveEncoder[Person].instance