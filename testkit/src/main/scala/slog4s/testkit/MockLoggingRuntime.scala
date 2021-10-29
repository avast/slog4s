package slog4s.testkit

import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import slog4s.shared.LoggingRuntime

/** Mock implementation of [[LoggingRuntime]]. It includes mock implementations
  * of both [[slog4s.LoggerFactory]] and [[slog4s.LoggingContext]] that are
  * integrated with each other.
  */
class MockLoggingRuntime[F[_]](val loggerFactory: MockLoggerFactory[F])(implicit
    val loggingContext: MockLoggingContext[F]
) extends LoggingRuntime[F]

object MockLoggingRuntime {
  def make[F[_]: Sync: Clock]: F[MockLoggingRuntime[F]] = {
    for {
      context <- MockLoggingContext.make
      factory <- MockLoggerFactory.withArguments(context.currentArguments)
    } yield new MockLoggingRuntime(factory)(context)
  }
}
