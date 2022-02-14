package pl.edu.agh.model

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedAsObjectEncoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import shapeless.Lazy

abstract class JsonSerializable[E] {
  implicit def jsonDecoder(implicit d: Lazy[DerivedDecoder[E]]): Decoder[E] =
    deriveDecoder[E]

  implicit def jsonEncoder(
    implicit
    d: Lazy[DerivedAsObjectEncoder[E]]
  ): Encoder[E] =
    deriveEncoder[E]
}
