package slog4s.console.internal

import java.io.PrintStream
import java.time.Instant

import cats.effect.Sync
import slog4s.console.Level
import slog4s.{Location, StructureBuilder}

private[console] class PlainFormatter[F[_]: Sync](
    printStream: PrintStream
) extends Formatter[F, String] {

  override def format(
      level: Level,
      logger: String,
      msg: String,
      throwable: Option[Throwable],
      args: Map[String, String],
      now: Instant,
      threadName: String,
      location: Location
  ): F[Unit] =
    Sync[F].delay {
      val builder = new StringBuilder
      builder
        .append(now)
        .append(" [")
        .append(threadName)
        .append("] ")
        .append(formattedLevel(level))
        .append(" ")
        .append(logger)
        .append(formatLocation(location))
        .append(formatArgs(args))
        .append(" : ")
        .append(msg)
      printStream.println(builder.mkString)
      throwable.foreach { value =>
        value.printStackTrace(printStream)
      }
    }

  private def formattedLevel(level: Level): String = {
    import scala.io.AnsiColor._
    level match {
      case Level.Trace => s"${MAGENTA}TRACE$RESET"
      case Level.Debug => s"${BLUE}DEBUG$RESET"
      case Level.Info  => s"${WHITE}INFO$RESET"
      case Level.Warn  => s"${YELLOW}WARN$RESET"
      case Level.Error => s"${RED}ERROR$RESET"
    }
  }

  private def formatLocation(location: Location): String = {
    location match {
      case Location.Code(file, line) => s" ($file:$line)"
      case Location.NotUsed          => ""
    }
  }

  private def formatArgs(args: Map[String, String]): String = {
    if (args.isEmpty) {
      ""
    } else {
      val builder = new StringBuilder()
      args.foreach {
        case (name, value) =>
          builder
            .append(" ")
            .append(name)
            .append("=")
            .append(value)
      }
      builder.mkString
    }
  }

}

private[console] object PlainFormatter {

  implicit val structureBuilder: StructureBuilder[String] =
    new StructureBuilder[String] {
      override def boolean(value: Boolean): String = value.toString
      override def long(value: Long): String = value.toString
      override def double(value: Double): String = value.toString
      override def string(value: String): String = value
      override def structure(
          name: String,
          attributes: Map[String, String]
      ): String = map(attributes)
      override def option(value: Option[String]): String =
        value.getOrElse("none")
      override def map(values: Map[String, String]): String =
        array(values.map(t => s"${t._1}=${t._2}"))
      override def array(values: Iterable[String]): String =
        values.mkString("[", ", ", "]")
    }

}
