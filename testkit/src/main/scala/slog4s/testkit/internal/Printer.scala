package slog4s.testkit.internal

import java.io.PrintStream

import cats.Show
import cats.effect.Sync
import slog4s.testkit.{Argument, LogEvent, LogEvents}
import slog4s.{Level, Location}

private[testkit] object Printer {

  def printLogEvents[F[_]: Sync](
      logEvents: LogEvents,
      printStream: PrintStream
  ): F[Unit] = {
    Sync[F].delay {
      logEvents.foldLeft(()) { (_, logEvent) =>
        printLogEventUnsafe(logEvent, printStream)
      }
    }
  }

  private def printLogEventUnsafe(
      logEvent: LogEvent,
      printStream: PrintStream
  ): Unit = {
    printStream.print(logEvent.timestamp)
    printStream.print(" ")
    printStream.print(formatLevel(logEvent.level))
    printStream.print(" ")
    printStream.print(logEvent.logger)
    printStream.print(" ")
    printStream.print(formatLocation(logEvent.location))
    printStream.print(
      Show[Argument].show(Argument.Map(logEvent.arguments): Argument)
    )
    printStream.print(" : ")
    printStream.print(logEvent.message)
    printStream.println()
    logEvent.exception.foreach { throwable =>
      throwable.printStackTrace(printStream)
    }
  }

  private def formatLevel(level: Level): String = {
    level match {
      case Level.Trace => "TRACE"
      case Level.Debug => "DEBUG"
      case Level.Info  => "INFO"
      case Level.Warn  => "WARN"
      case Level.Error => "ERROR"
    }
  }

  private def formatLocation(location: Location): String = {
    location match {
      case Location.Code(file, line) => s"$file:$line "
      case Location.NotUsed          => ""
    }
  }
}
