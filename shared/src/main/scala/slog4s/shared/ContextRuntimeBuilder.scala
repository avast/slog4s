package slog4s.shared

/**
  * Makes a new instance of [[ContextRuntime]] for a specified type.
  */
trait ContextRuntimeBuilder[F[_]] {
  def make[T](empty: T): F[ContextRuntime[F, T]]
}
