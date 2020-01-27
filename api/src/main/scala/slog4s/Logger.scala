package slog4s

import cats.Applicative

/**
  * Basic Logger API. Provides different builders for different log levels.
  * @tparam F
  */
trait Logger[F[_]] {
  def debug: LevelLogBuilder[F]
  def error: LevelLogBuilder[F]
  def info: LevelLogBuilder[F]
  def trace: LevelLogBuilder[F]
  def warn: LevelLogBuilder[F]
}

object Logger {
  def noop[F[_]: Applicative]: Logger[F] =
    new Logger[F] {
      override val debug: LevelLogBuilder[F] = LevelLogBuilder.noop

      override val error: LevelLogBuilder[F] = LevelLogBuilder.noop

      override val info: LevelLogBuilder[F] = LevelLogBuilder.noop

      override val trace: LevelLogBuilder[F] = LevelLogBuilder.noop

      override val warn: LevelLogBuilder[F] = LevelLogBuilder.noop
    }
}
