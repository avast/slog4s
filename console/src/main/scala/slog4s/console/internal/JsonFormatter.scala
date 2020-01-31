package slog4s.console.internal

import java.io.PrintStream
import java.time.Instant

import cats.effect.Sync
import io.circe.Json
import slog4s.console.{JsonArgs, Level}
import slog4s.{Location, StructureBuilder}

private[console] class JsonFormatter[F[_]: Sync](printStream: PrintStream)
    extends Formatter[F, Json] {
  override def format(
      level: Level,
      logger: String,
      msg: String,
      throwable: Option[Throwable],
      args: Map[String, Json],
      now: Instant,
      threadName: String,
      location: Location
  ): F[Unit] = Sync[F].delay {
    val allFields =
      args ++ Map(
        "level" -> encodeLevel(level),
        "logger" -> Json.fromString(logger),
        "message" -> Json.fromString(msg),
        "timestamp" -> Json.fromString(now.toString),
        "thread" -> Json.fromString(threadName)
      ) ++ encodeLocation(location) ++ (throwable match {
        case None        => Map.empty
        case Some(value) => Map("exception" -> encodeException(value))
      })
    val json = Json.obj(allFields.toSeq: _*)
    printStream.println(json.noSpacesSortKeys)
  }

  private def encodeLevel(level: Level): Json = level match {
    case Level.Trace => Json.fromString("TRACE")
    case Level.Debug => Json.fromString("DEBUG")
    case Level.Info  => Json.fromString("INFO")
    case Level.Warn  => Json.fromString("WARN")
    case Level.Error => Json.fromString("ERROR")
  }
  private def encodeLocation(location: Location): JsonArgs = {
    location match {
      case Location.Code(file, line) =>
        Map("file" -> Json.fromString(file), "line" -> Json.fromInt(line))
      case Location.NotUsed => Map.empty
    }
  }

  private def encodeException(throwable: Throwable): Json = {
    val fields = List(
      "message" -> Option(throwable.getMessage)
        .map(Json.fromString)
        .getOrElse(Json.Null),
      "stack_trace" -> Json.arr(
        throwable.getStackTrace
          .map(element => Json.fromString(element.toString))
          .toIndexedSeq: _*
      )
    )
    Option(throwable.getCause) match {
      case Some(value) =>
        Json.obj(("cause" -> encodeException(value)) :: fields: _*)
      case None => Json.obj(fields: _*)

    }
  }
}

private[console] object JsonFormatter {
  implicit val structureBuilder: StructureBuilder[Json] =
    new StructureBuilder[Json] {
      override def boolean(value: Boolean): Json = Json.fromBoolean(value)
      override def long(value: Long): Json = Json.fromLong(value)
      override def double(value: Double): Json =
        Json.fromDouble(value).getOrElse(Json.fromString(value.toString))
      override def string(value: String): Json = Json.fromString(value)
      override def structure(
          name: String,
          attributes: Map[String, Json]
      ): Json = Json.obj(attributes.toSeq: _*)
      override def option(value: Option[Json]): Json =
        value.getOrElse(Json.Null)
      override def map(values: Map[String, Json]): Json =
        Json.obj(values.toSeq: _*)
      override def array(values: Iterable[Json]): Json =
        Json.arr(values.toSeq: _*)
    }
}
