package slog4s.shared

trait ContextRuntime[F[_], T] {
  implicit def use: UseContext[F, T]
  implicit def as: AsContext[F, T]
}
