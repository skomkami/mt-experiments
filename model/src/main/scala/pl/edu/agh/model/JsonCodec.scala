package pl.edu.agh.model
//
import io.circe.Decoder
import io.circe.Encoder
import io.circe.*
//import io.circe.Codec.AsObject
import io.circe.syntax.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.EncoderOps
//
//import scala.deriving.Mirror
//

object JsonCodec:
  inline def toJson[T: Encoder](obj: T): String = obj.asJson.noSpaces

  inline def fromJsonSafe[T](str: String)(implicit decoder: Decoder[T]): Either[Error, T] = decode[T](str)
  inline def fromJson[T](str: String)(implicit decoder: Decoder[T]): T = decode[T](str) match {
    case Right(value) => value
    case Left(error)  => throw new Throwable(s"Error: ${error.getMessage}")
  }

//trait JsonDeserializable[T] extends Codec.AsObject[T] {
//
//  def fromJson(    obj: String  ): Either[Throwable, T] = {
//    parse(obj).flatMap(decodeJson)
//  }
//  def unsafeFromJson(obj: String): T = {
//    parse(obj).flatMap(decodeJson) match {
//      case Right(value) => value
//      case Left(error)  => throw new Throwable(s"Error: ${error.getMessage}")
//    }
//  }
//}
//
//trait JsonSerializable[T] extends Encoder.AsObject[T] {
//  def toJson(obj: T): String = encodeObject(obj).asJson.noSpaces
//}
//
//trait JsonCodec[T]
//    extends JsonDeserializable[T]
//    with JsonSerializable[T]
//
//object JsonCodec {
//  def apply[A](implicit instance: JsonCodec[A]): AsObject[A] = instance
//  inline final def derived[A](using inline A: Mirror.Of[A]): Codec.AsObject[A] =
//    Codec.derived[A]
//}
