package slog4s.console

import slog4s.LoggingContext
import slog4s.shared.{MapLoggingContext, UseContext}

object ConsoleLoggingContext {
  def plain[F[_]](
      implicit useContext: UseContext[F, PlainArgs]
  ): LoggingContext[F] = {
    import slog4s.console.internal.PlainFormatter._
    new MapLoggingContext()
  }

  def json[F[_]](
      implicit useContext: UseContext[F, JsonArgs]
  ): LoggingContext[F] = {
    import slog4s.console.internal.JsonFormatter._
    new MapLoggingContext()
  }
}
