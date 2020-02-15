package slog4s.console

import slog4s.LoggingContext
import slog4s.shared.{MapLoggingContext, UseContext}

/**
  * Console implementation of [[LoggingContext]].
  */
object ConsoleLoggingContext {

  /**
    * Creates a [[LoggingContext]] specific for plain format.
    */
  def plain[F[_]](
      implicit useContext: UseContext[F, PlainArgs]
  ): LoggingContext[F] = {
    import slog4s.console.internal.PlainFormatter._
    new MapLoggingContext()
  }

  /**
    * Creates a [[LoggingContext]] specific for JSON format.
    */
  def json[F[_]](
      implicit useContext: UseContext[F, JsonArgs]
  ): LoggingContext[F] = {
    import slog4s.console.internal.JsonFormatter._
    new MapLoggingContext()
  }
}
