package slog4s.slf4j

import cats.mtl.ApplicativeLocal
import slog4s.LoggingContext

object Slf4jContext {

  /**
    * Makes an instance of [[slog4s.LoggingContext]] that works well with [[Slf4jFactory]]. It is
    * backed by [[cats.mtl.ApplicativeLocal]].
    */
  def make[F[_]](
      implicit applicativeLocal: ApplicativeLocal[F, Slf4jArgs]
  ): LoggingContext[F] = {
    new Slf4jLoggingContext[F]
  }
}
