package slog4s.shared

import cats.kernel.Monoid
import cats.mtl.ApplicativeLocal

/**
  * Simple typeclass used to inject contextual data into an effect.
  * It's a helper typeclass used by [[MapLoggingContext]].
  */
trait UseContext[F[_], T] {

  /**
    * Injects contextual value into an effect. It has append-like semantics.
    */
  def use[V](value: T)(fv: F[V]): F[V]
}

object UseContext {
  implicit def fromApplicativeLocal[F[_], T: Monoid](
      implicit local: ApplicativeLocal[F, T]
  ): UseContext[F, T] = new UseContext[F, T] {
    override def use[V](value: T)(fv: F[V]): F[V] = {
      local.local(v => Monoid[T].combine(v, value))(fv)
    }
  }
}
