package slog4s.shared

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
}
