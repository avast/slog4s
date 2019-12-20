package slog

trait LogBuilder[F[_]] {
  def log(msg: String): F[Unit]
  def log(ex: Throwable, msg: String): F[Unit]
  def withArg[T: StructureEncoder](key: String, value: => T): LogBuilder[F]
}
