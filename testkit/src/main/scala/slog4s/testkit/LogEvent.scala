package slog4s.testkit

import java.time.Instant

import slog4s.{Level, Location}

/** Materialized log statement event.
  */
final case class LogEvent(
    message: String,
    exception: Option[Throwable],
    arguments: Map[String, Argument],
    location: Location,
    timestamp: Instant,
    level: Level,
    logger: String
)
