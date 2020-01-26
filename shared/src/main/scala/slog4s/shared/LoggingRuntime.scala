package slog4s.shared

import slog4s.{LoggerFactory, LoggingContext}

trait LoggingRuntime[F[_]] {
  def loggerFactory: LoggerFactory[F]
  implicit def loggingContext: LoggingContext[F]
}
