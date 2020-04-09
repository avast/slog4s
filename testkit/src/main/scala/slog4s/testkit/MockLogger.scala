package slog4s.testkit

import java.io.PrintStream
import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.data.Chain
import cats.effect.concurrent.Ref
import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, Monad}
import slog4s._
import slog4s.testkit.internal.{ArgumentStructureBuilder, Printer}

/**
  * Mock implementation of [[Logger]] useful for unit testing.
  */
class MockLogger[F[_]: Sync: Clock](
    logEvents: Ref[F, LogEvents],
    name: String,
    getArguments: F[Arguments]
) extends Logger[F] {
  override def debug: LevelLogBuilder[F] = make(Level.Debug)

  override def error: LevelLogBuilder[F] = make(Level.Error)

  override def info: LevelLogBuilder[F] = make(Level.Info)

  override def trace: LevelLogBuilder[F] = make(Level.Trace)

  override def warn: LevelLogBuilder[F] = make(Level.Warn)

  /**
    * Gets all log events produced by this logger.
    */
  def events: F[LogEvents] = logEvents.get.map(_.filter(_.logger == name))

  /**
    * Removes all log events produced by this logger.
    */
  def drain: F[Unit] = logEvents.update(_.filter(_.logger != name))

  /**
    * Prints all log events produced by this logger to provided stream.
    */
  def print(printStream: PrintStream = System.out): F[Unit] = {
    events.flatMap { allEvents =>
      Printer.printLogEvents(
        allEvents,
        printStream
      )
    }
  }

  private def make(level: Level): LevelLogBuilder[F] = {
    val builder = new LogEventBuilder(logEvents, level, name, getArguments)
    new MockLevelLogBuilder[F](builder)
  }
}

object MockLogger {
  def make[F[_]: Sync: Clock](name: String): F[MockLogger[F]] = {
    for {
      events <- Ref.of(List.empty[LogEvent])
    } yield new MockLogger(events, name, Applicative[F].pure(Map.empty))
  }
}

private class LogEventBuilder[F[_]: Monad: Clock](
    logEvents: Ref[F, LogEvents],
    level: Level,
    logger: String,
    getArguments: F[Arguments]
) {
  def make(msg: String, ex: Option[Throwable], arguments: Arguments)(
      implicit location: Location
  ): F[Unit] = {
    for {
      contextArguments <- getArguments
      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      _ <- logEvents.update { allEvents =>
        val newEvent =
          LogEvent(
            msg,
            ex,
            contextArguments ++ arguments,
            location,
            Instant.ofEpochMilli(timestamp),
            level,
            logger
          )
        allEvents :+ newEvent
      }
    } yield ()
  }
}

private class MockLevelLogBuilder[F[_]](
    logEventBuilder: LogEventBuilder[F]
) extends LevelLogBuilder[F] {
  override def apply(msg: String)(implicit location: Location): F[Unit] =
    logEventBuilder.make(msg, None, Map.empty)

  override def apply(ex: Throwable, msg: String)(
      implicit location: Location
  ): F[Unit] = logEventBuilder.make(msg, Some(ex), Map.empty)

  override def withArg[T: LogEncoder](key: String, value: => T): LogBuilder[F] =
    new MockLogBuilder(
      logEventBuilder,
      Map(key -> LogEncoder[T].encode(value)(ArgumentStructureBuilder))
    )
  override def whenEnabled: WhenEnabledLogBuilder[F] =
    new MockWhenEnabledLogBuilder(logEventBuilder)
}

private class MockLogBuilder[F[_]](
    logEventBuilder: LogEventBuilder[F],
    arguments: Arguments
) extends LogBuilder[F] {
  override def log(msg: String)(implicit location: Location): F[Unit] =
    logEventBuilder.make(msg, None, arguments)

  override def log(ex: Throwable, msg: String)(
      implicit location: Location
  ): F[Unit] = logEventBuilder.make(msg, Some(ex), arguments)

  override def withArg[T: LogEncoder](key: String, value: => T): LogBuilder[F] =
    new MockLogBuilder(
      logEventBuilder,
      arguments
        .updated(key, LogEncoder[T].encode(value)(ArgumentStructureBuilder))
    )
}

private class MockWhenEnabledLogBuilder[F[_]](
    logEventBuilder: LogEventBuilder[F]
) extends WhenEnabledLogBuilder[F] {
  override def apply(f: LogBuilder[F] => F[Unit]): F[Unit] =
    f(new MockLogBuilder[F](logEventBuilder, Map.empty))
}
