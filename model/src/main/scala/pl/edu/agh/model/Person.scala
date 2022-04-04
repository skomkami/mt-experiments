package pl.edu.agh.model

case class Person(name: String, address: String, email: String)

case object Person extends JsonCodec[Person]
