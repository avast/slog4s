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
      private[this] val F = Applicative[F]

      private[this] val log: LogBuilder[F] = new LogBuilder[F] { self =>
        override def log(msg: String)(implicit location: Location): F[Unit] =
          F.unit

        override def log(ex: Throwable, msg: String)(
            implicit location: Location
        ): F[Unit] = F.unit

        override def withArg[T: LogEncoder](
            key: String,
            value: => T
        ): LogBuilder[F] = self
      }

      private[this] val whenEnabledLogBuilder: WhenEnabledLogBuilder[F] =
        new WhenEnabledLogBuilder[F] {
          override def apply(f: LogBuilder[F] => F[Unit]): F[Unit] = F.unit
        }

      private[this] val level: LevelLogBuilder[F] = new LevelLogBuilder[F] {
        override def apply(msg: String)(implicit location: Location): F[Unit] =
          F.unit

        override def apply(ex: Throwable, msg: String)(
            implicit location: Location
        ): F[Unit] = F.unit

        override def withArg[T: LogEncoder](
            key: String,
            value: => T
        ): LogBuilder[F] = log

        override def whenEnabled: WhenEnabledLogBuilder[F] =
          whenEnabledLogBuilder
      }

      override def debug: LevelLogBuilder[F] = level

      override def error: LevelLogBuilder[F] = level

      override def info: LevelLogBuilder[F] = level

      override def trace: LevelLogBuilder[F] = level

      override def warn: LevelLogBuilder[F] = level
    }
}
