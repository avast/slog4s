package slog4s.shared

import cats.kernel.Monoid
import cats.mtl.ApplicativeLocal

/** Simple typeclass used to inject contextual data into an effect. It's a
  * helper typeclass used by [[MapLoggingContext]].
  */
trait UseContext[F[_], T] {

  /** Injects contextual value into an effect.
    */
  def use[V](value: T)(fv: F[V]): F[V] = update(_ => value)(fv)

  /** Updates contextual value inside an effect.
    */
  def update[V](f: T => T)(fv: F[V]): F[V]
}

object UseContext {

  def apply[F[_], T](implicit ev: UseContext[F, T]): UseContext[F, T] = ev

  implicit def fromApplicativeLocal[F[_], T](implicit
      local: ApplicativeLocal[F, T]
  ): UseContext[F, T] = new UseContext[F, T] {
    override def update[V](f: T => T)(fv: F[V]): F[V] = {
      local.local(f)(fv)
    }
  }
}
