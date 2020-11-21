package slog4s.console

import java.io.PrintStream

import cats.effect.{Clock, Concurrent}
import cats.syntax.flatMap._
import cats.syntax.functor._
import slog4s.console.internal._
import slog4s.shared.{
  AsContext,
  ContextRuntimeBuilder,
  LoggingRuntime,
  UseContext
}

/** Factory used for console logging.
  */
object ConsoleFactory {

  /** Bounds a builder to a specific effect type.
    */
  def apply[F[_]: Concurrent: Clock]: ConsoleFactoryBuilder[F] =
    new ConsoleFactoryBuilder[F](System.out)

  final class ConsoleFactoryBuilder[F[_]] private[console] (
      printStream: PrintStream
  )(implicit
      F: Concurrent[F],
      C: Clock[F]
  ) {

    /** Makes a new console based [[slog4s.shared.LoggingRuntime]] with desired format.
      */
    def makeFromBuilder(
        format: Format,
        consoleConfig: ConsoleConfig[F],
        contextRuntimeBuilder: ContextRuntimeBuilder[F]
    ): F[LoggingRuntime[F]] = {

      format match {
        case Format.Plain =>
          contextRuntimeBuilder.make(PlainArgs.empty).flatMap {
            contextRuntime =>
              import contextRuntime._
              plain(consoleConfig)
          }
        case Format.Json =>
          contextRuntimeBuilder.make(JsonArgs.empty).flatMap { contextRuntime =>
            import contextRuntime._
            json(consoleConfig)
          }
      }

    }

    /** Makes a new console based [[slog4s.shared.LoggingRuntime]] with plain format.
      */
    def plain(consoleConfig: ConsoleConfig[F])(implicit
        asContext: AsContext[F, PlainArgs],
        useContext: UseContext[F, PlainArgs]
    ): F[LoggingRuntime[F]] = {
      import PlainFormatter._
      LoggingRuntimeImpl
        .make(
          consoleConfig,
          new PlainFormatter(printStream),
          ConsoleLoggingContext.plain
        )
        .widen
    }

    /** Makes a new console based [[slog4s.shared.LoggingRuntime]] with JSON format.
      */
    def json(consoleConfig: ConsoleConfig[F])(implicit
        asContext: AsContext[F, JsonArgs],
        useContext: UseContext[F, JsonArgs]
    ): F[LoggingRuntime[F]] = {
      import JsonFormatter._
      LoggingRuntimeImpl
        .make(
          consoleConfig,
          new JsonFormatter(printStream),
          ConsoleLoggingContext.json
        )
        .widen
    }

    private[console] def withPrintStream(
        printStream: PrintStream
    ): ConsoleFactoryBuilder[F] = {
      new ConsoleFactoryBuilder(printStream)
    }
  }

}
