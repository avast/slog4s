package slog4s.shared

import cats.mtl.ApplicativeAsk

/**
  * Simple typeclass used to extract contextual data from an effect.
  */
trait AsContext[F[_], T] {

  /**
    * Gets context value from an effect.
    */
  def get: F[T]
}

object AsContext {
  implicit def fromApplicativeAsk[F[_], T](
      implicit ask: ApplicativeAsk[F, T]
  ): AsContext[F, T] = new AsContext[F, T] {
    override def get: F[T] = ask.ask
  }
}
