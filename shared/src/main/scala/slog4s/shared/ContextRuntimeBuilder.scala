package slog4s.shared

import cats.Applicative

/** Makes a new instance of [[ContextRuntime]] for a specified type.
  */
trait ContextRuntimeBuilder[F[_]] {
  def make[T](empty: T): F[ContextRuntime[F, T]]
}

object ContextRuntimeBuilder {
  def alwaysEmpty[F[_]: Applicative]: ContextRuntimeBuilder[F] =
    new ContextRuntimeBuilder[F] {
      override def make[T](empty: T): F[ContextRuntime[F, T]] =
        Applicative[F].pure(ContextRuntime.alwaysEmpty(empty))
    }
}
