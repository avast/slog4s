package slog4s.slf4j

import slog4s.{LoggingContext, LogEncoder}
import MarkerStructureBuilder._
import cats.mtl.ApplicativeLocal

private[slf4j] class Slf4jLoggingContext[F[_]](
    implicit
    applicativeLocal: ApplicativeLocal[F, Slf4jArgs]
) extends LoggingContext[F] {

  final class Builder(args: Map[String, Any])
      extends LoggingContext.LoggingBuilder[F] {
    override def use[T](fv: F[T]): F[T] = applicativeLocal.scope(args)(fv)

    override def withArg[T: LogEncoder](
        key: String,
        value: T
    ): LoggingContext.LoggingBuilder[F] =
      new Builder(args.updated(key, LogEncoder[T].encode(value)))
  }

  override def withArg[T: LogEncoder](
      key: String,
      value: T
  ): LoggingContext.LoggingBuilder[F] =
    new Builder(Map(key -> LogEncoder[T].encode(value)))
}
