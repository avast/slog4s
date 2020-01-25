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
  implicit def fromApplicativeLocal[F[_], T](
      implicit local: ApplicativeLocal[F, Map[String, T]]
  ): UseContext[F, Map[String, T]] = new UseContext[F, Map[String, T]] {
    override def use[V](value: Map[String, T])(fv: F[V]): F[V] = {
      local.local(_ ++ value)(fv)
    }
  }
}
