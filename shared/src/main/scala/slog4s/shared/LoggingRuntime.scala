package slog4s.shared

import slog4s.{LoggerFactory, LoggingContext}

/** Helper trait that is used by factories to build both [[LoggerFactory]] and [[LoggingContext]].
  */
trait LoggingRuntime[F[_]] {
  def loggerFactory: LoggerFactory[F]
  implicit def loggingContext: LoggingContext[F]
}
