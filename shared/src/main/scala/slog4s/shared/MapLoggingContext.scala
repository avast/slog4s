package slog4s.shared

import slog4s._

/**
  * Common [[LoggingContext]] implementation. Should be good enough for the most of the use cases.
  */
class MapLoggingContext[F[_], O: StructureBuilder](
    implicit useContext: UseContext[F, Map[String, O]]
) extends LoggingContext[F] {

  override def withArg[T: LogEncoder](
      key: String,
      value: T
  ): LoggingContext.LoggingBuilder[F] = {
    new Builder(Map(key -> LogEncoder[T].encode(value)))
  }

  private final class Builder(values: Map[String, O])
      extends LoggingContext.LoggingBuilder[F] {

    override def use[T](fv: F[T]): F[T] = useContext.update(_ ++ values)(fv)

    override def withArg[T: LogEncoder](
        key: String,
        value: T
    ): LoggingContext.LoggingBuilder[F] =
      new Builder(values.updated(key, LogEncoder[T].encode(value)))
  }
}
