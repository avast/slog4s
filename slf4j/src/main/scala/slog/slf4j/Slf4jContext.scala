package slog.slf4j

import cats.mtl.ApplicativeLocal
import slog.LoggingContext

object Slf4jContext {
  def make[F[_]](
      implicit applicativeLocal: ApplicativeLocal[F, Slf4jArgs]
  ): LoggingContext[F] = {
    new Slf4jLoggingContext[F]
  }
}
