package slog4s.console.internal

import cats.effect.{Clock, Concurrent, Sync}
import cats.syntax.functor._
import slog4s.console.ConsoleConfig
import slog4s.shared.{AsContext, LoggingRuntime}
import slog4s.{Logger, LoggerFactory, LoggingContext, StructureBuilder}
import cats.effect.std.Semaphore

private[console] class LoggingRuntimeImpl[F[
    _
]: Sync: Clock, T: StructureBuilder](
    consoleConfig: ConsoleConfig[F],
    semaphore: Semaphore[F],
    underlying: Formatter[F, T],
    context: LoggingContext[F]
)(implicit asContext: AsContext[F, Map[String, T]])
    extends LoggingRuntime[F] {
  override val loggerFactory: LoggerFactory[F] = new LoggerFactory[F] {
    private[this] val formatter =
      new SyncFormatter(semaphore, underlying)
    override def make(name: String): Logger[F] =
      new ConsoleLogger[F, T](
        name,
        consoleConfig,
        formatter
      )
  }
  override implicit val loggingContext: LoggingContext[F] = context
}

private[console] object LoggingRuntimeImpl {
  def make[F[_]: Concurrent: Clock, T: StructureBuilder](
      consoleConfig: ConsoleConfig[F],
      underlying: Formatter[F, T],
      context: LoggingContext[F]
  )(implicit
      asContext: AsContext[F, Map[String, T]]
  ): F[LoggingRuntimeImpl[F, T]] = {
    Semaphore[F](1).map { semaphore =>
      new LoggingRuntimeImpl(consoleConfig, semaphore, underlying, context)
    }
  }
}
