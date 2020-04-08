package slog4s.testkit

import cats.effect.concurrent.Ref
import cats.effect.{Bracket, Sync}
import cats.syntax.functor._
import slog4s.testkit.internal.ArgumentStructureBuilder
import slog4s.{LogEncoder, LoggingContext}

/**
  * Mock implementation of [[LoggingContext]]. It's using [[cats.effect.concurrent.Ref]] which
  * means it DOES NOT support context propagation for concurrent programs.
  */
class MockLoggingContext[F[_]](storage: Ref[F, Arguments])(
    implicit F: Bracket[F, Throwable]
) extends LoggingContext[F] {
  override def withArg[T: LogEncoder](
      key: String,
      value: T
  ): LoggingContext.LoggingBuilder[F] =
    new MockLoggingContextBuilder[F](
      storage,
      Map(key -> LogEncoder[T].encode(value)(ArgumentStructureBuilder))
    )

  /**
    * Gets arguments in current context.
    */
  def currentArguments: F[Arguments] = storage.get
}

object MockLoggingContext {
  def make[F[_]: Sync]: F[MockLoggingContext[F]] = {
    for {
      storage <- Ref.of[F, Arguments](Map.empty)
    } yield new MockLoggingContext(storage)
  }
}

private class MockLoggingContextBuilder[F[_]](
    storage: Ref[F, Arguments],
    arguments: Arguments
)(implicit F: Bracket[F, Throwable])
    extends LoggingContext.LoggingBuilder[F] {
  override def use[T](fv: F[T]): F[T] = {
    F.bracket(storage.getAndUpdate(_ ++ arguments))(_ => fv)(storage.set)
  }

  override def withArg[T: LogEncoder](
      key: String,
      value: T
  ): LoggingContext.LoggingBuilder[F] =
    new MockLoggingContextBuilder[F](
      storage,
      arguments
        .updated(
          key,
          LogEncoder[T].encode(value)(ArgumentStructureBuilder)
        )
    )
}
