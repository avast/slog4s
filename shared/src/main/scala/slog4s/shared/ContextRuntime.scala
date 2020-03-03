package slog4s.shared

import cats.Applicative

/**
  * Helper trait that is returned by [[slog4s.LoggingContext]] factories.
  */
trait ContextRuntime[F[_], T] {
  implicit def use: UseContext[F, T]
  implicit def as: AsContext[F, T]
}

object ContextRuntime {
  def apply[F[_], T](
      implicit
      useContext: UseContext[F, T],
      asContext: AsContext[F, T]
  ): ContextRuntime[F, T] =
    new ContextRuntime[F, T] {
      override implicit val use: UseContext[F, T] = useContext
      override implicit val as: AsContext[F, T] = asContext
    }

  def alwaysEmpty[F[_], T](
      value: T
  )(implicit F: Applicative[F]): ContextRuntime[F, T] = {
    new ContextRuntime[F, T] {
      override implicit val use: UseContext[F, T] = new UseContext[F, T] {
        override def update[V](f: T => T)(fv: F[V]): F[V] = fv
      }
      override implicit val as: AsContext[F, T] = new AsContext[F, T] {
        override def get: F[T] = F.pure(value)
      }
    }
  }
}
