package slog4s.shared

import cats.mtl.ApplicativeAsk

/** Simple typeclass used to extract contextual data from an effect.
  */
trait AsContext[F[_], T] {

  /** Gets context value from an effect.
    */
  def get: F[T]
}

object AsContext {
  def apply[F[_], T](implicit ev: AsContext[F, T]): AsContext[F, T] = ev

  implicit def fromApplicativeAsk[F[_], T](implicit
      ask: ApplicativeAsk[F, T]
  ): AsContext[F, T] = new AsContext[F, T] {
    override def get: F[T] = ask.ask
  }
}
