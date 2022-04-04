package pl.edu.agh.model

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedAsObjectEncoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import shapeless.Lazy
import io.circe.parser._
import io.circe.syntax.EncoderOps

abstract class JsonCodec[T] {
  implicit def jsonDecoder(implicit d: Lazy[DerivedDecoder[T]]): Decoder[T] =
    deriveDecoder[T]

  implicit def jsonEncoder(
    implicit
    d: Lazy[DerivedAsObjectEncoder[T]]
  ): Encoder[T] =
    deriveEncoder[T]

  def toJson(obj: T)(implicit
                     d: Lazy[DerivedAsObjectEncoder[T]]): String =
    obj.asJson.noSpaces

  def fromJson(
    obj: String
  )(implicit d: Lazy[DerivedDecoder[T]]): Either[Throwable, T] = {
    parse(obj).flatMap(jsonDecoder.decodeJson)
  }

  def unsafeFromJson(obj: String)(implicit d: Lazy[DerivedDecoder[T]]): T = {
    parse(obj).flatMap(jsonDecoder.decodeJson) match {
      case Right(value) => value
      case Left(error)  => throw new Throwable(s"Error: ${error.getMessage}")
    }
  }

}
