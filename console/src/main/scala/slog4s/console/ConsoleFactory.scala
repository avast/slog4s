package slog4s.console

import java.io.PrintStream

import cats.effect.concurrent.Semaphore
import cats.effect.{Clock, Concurrent}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.Json
import slog4s.console.internal.{
  ConsoleLogger,
  JsonFormatter,
  PlainFormatter,
  SyncFormatter
}
import slog4s.shared.{
  AsContext,
  ContextRuntimeBuilder,
  LoggingRuntime,
  UseContext
}
import slog4s.{Logger, LoggerFactory, LoggingContext}

object ConsoleFactory {

  def apply[F[_]: Concurrent: Clock]: ConsoleFactoryBuilder[F] =
    new ConsoleFactoryBuilder[F](System.out)

  final class ConsoleFactoryBuilder[F[_]] private[console] (
      printStream: PrintStream
  )(
      implicit F: Concurrent[F],
      C: Clock[F]
  ) {

    def make(
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

    def plain(consoleConfig: ConsoleConfig[F])(
        implicit asContext: AsContext[F, PlainArgs],
        useContext: UseContext[F, PlainArgs]
    ): F[LoggingRuntime[F]] = Semaphore(1).map { semaphore =>
      new LoggingRuntime[F] {
        override val loggerFactory: LoggerFactory[F] = new LoggerFactory[F] {
          import PlainFormatter._
          private[this] val formatter =
            new SyncFormatter(semaphore, new PlainFormatter(printStream))
          override def make(name: String): Logger[F] =
            new ConsoleLogger[F, String](
              name,
              consoleConfig,
              formatter
            )
        }
        override implicit def loggingContext: LoggingContext[F] =
          ConsoleLoggingContext.plain
      }
    }

    def json(consoleConfig: ConsoleConfig[F])(
        implicit asContext: AsContext[F, JsonArgs],
        useContext: UseContext[F, JsonArgs]
    ): F[LoggingRuntime[F]] = Semaphore(1).map { semaphore =>
      new LoggingRuntime[F] {
        override val loggerFactory: LoggerFactory[F] = new LoggerFactory[F] {
          import JsonFormatter._
          private[this] val formatter =
            new SyncFormatter(semaphore, new JsonFormatter(printStream))
          override def make(name: String): Logger[F] =
            new ConsoleLogger[F, Json](
              name,
              consoleConfig,
              formatter
            )
        }
        override implicit def loggingContext: LoggingContext[F] =
          ConsoleLoggingContext.json
      }
    }

    private[console] def withPrintStream(
        printStream: PrintStream
    ): ConsoleFactoryBuilder[F] = {
      new ConsoleFactoryBuilder(printStream)
    }
  }

}
