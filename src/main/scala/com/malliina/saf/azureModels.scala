package com.malliina.saf

import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}

trait NamedEnum:
  def name: String
trait NamedEnumCompanion[T <: NamedEnum]:
  def all: Seq[T]
  implicit val json: Codec[T] = Codec.from(
    Decoder.decodeString.emap(s =>
      all
        .find(_.name == s)
        .toRight(s"Invalid value: '$s'. Expected one of: ${all.map(_.name).mkString(", ")}.")
    ),
    Encoder.encodeString.contramap(_.name)
  )

enum BindingType(val name: String) extends NamedEnum:
  case HttpTrigger extends BindingType("httpTrigger")
  case Http extends BindingType("http")
object BindingType extends NamedEnumCompanion[BindingType]:
  override val all: Seq[BindingType] = BindingType.values

enum Direction(val name: String) extends NamedEnum:
  case In extends Direction("in")
  case Out extends Direction("out")
object Direction extends NamedEnumCompanion[Direction]:
  override val all: Seq[Direction] = Direction.values

enum Method(val name: String) extends NamedEnum:
  case GET extends Method("GET")
  case POST extends Method("POST")
object Method extends NamedEnumCompanion[Method]:
  override val all: Seq[Method] = Method.values

enum AuthLevel(val name: String) extends NamedEnum:
  case Anonymous extends AuthLevel("ANONYMOUS")
object AuthLevel extends NamedEnumCompanion[AuthLevel]:
  override val all: Seq[AuthLevel] = AuthLevel.values

case class Binding(
  `type`: BindingType,
  direction: Direction,
  name: String,
  methods: Option[Seq[Method]],
  authLevel: Option[AuthLevel]
)
object Binding:
//  implicit val jsonConf: Configuration = Configuration.default
  val encoder = deriveEncoder[Binding].mapJson(_.deepDropNullValues)
  implicit val json: Codec[Binding] = Codec.from(deriveDecoder[Binding], encoder)
  def outHttp: Binding = Binding(BindingType.Http, Direction.Out, "$return", None, None)

case class FunctionJson(scriptFile: String, entryPoint: String, bindings: Seq[Binding])
object FunctionJson:
  implicit val json: Codec[FunctionJson] = deriveCodec[FunctionJson]
