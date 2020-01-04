package slog4s.slf4j

import cats.mtl.ApplicativeLocal
import slog4s.LoggingContext

object Slf4jContext {
  def make[F[_]](
      implicit applicativeLocal: ApplicativeLocal[F, Slf4jArgs]
  ): LoggingContext[F] = {
    new Slf4jLoggingContext[F]
  }
}
