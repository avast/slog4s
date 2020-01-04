package slog4s

trait LogBuilder[F[_]] {
  def log(msg: String): F[Unit]
  def log(ex: Throwable, msg: String): F[Unit]
  def withArg[T: LogEncoder](key: String, value: => T): LogBuilder[F]
}
