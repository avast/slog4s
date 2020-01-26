package slog4s.shared

trait ContextRuntimeBuilder[F[_]] {
  def make[T](empty: T): F[ContextRuntime[F, T]]
}
