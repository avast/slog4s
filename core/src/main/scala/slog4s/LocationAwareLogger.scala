package slog4s

import cats.Applicative

/**
  * Logger API that is aware of logging statement location such as file name and
  * line number. Methods of this trait are typically not called directly but rather
  * by a macro.
  * @tparam F
  */
trait LocationAwareLogger[F[_]] {
  def debug(filename: String, line: Int): LevelLogBuilder[F]
  def error(filename: String, line: Int): LevelLogBuilder[F]
  def info(filename: String, line: Int): LevelLogBuilder[F]
  def trace(filename: String, line: Int): LevelLogBuilder[F]
  def warn(filename: String, line: Int): LevelLogBuilder[F]
}

object LocationAwareLogger {
  def noop[F[_]: Applicative]: LocationAwareLogger[F] =
    new LocationAwareLogger[F] {
      private[this] val F = Applicative[F]

      private[this] val log: LogBuilder[F] = new LogBuilder[F] { self =>
        override def log(msg: String): F[Unit] = F.unit

        override def log(ex: Throwable, msg: String): F[Unit] = F.unit

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
        override def apply(msg: String): F[Unit] = F.unit

        override def apply(ex: Throwable, msg: String): F[Unit] = F.unit

        override def withArg[T: LogEncoder](
            key: String,
            value: => T
        ): LogBuilder[F] = log

        override def whenEnabled: WhenEnabledLogBuilder[F] =
          whenEnabledLogBuilder
      }

      override def debug(filename: String, line: Int): LevelLogBuilder[F] =
        level

      override def error(filename: String, line: Int): LevelLogBuilder[F] =
        level

      override def info(filename: String, line: Int): LevelLogBuilder[F] = level

      override def trace(filename: String, line: Int): LevelLogBuilder[F] =
        level

      override def warn(filename: String, line: Int): LevelLogBuilder[F] = level
    }
}
