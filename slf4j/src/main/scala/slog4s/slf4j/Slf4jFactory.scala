package slog4s.slf4j

import cats.effect.Sync
import cats.syntax.functor._
import org.slf4j.Marker
import slog4s.shared.{ContextRuntime, ContextRuntimeBuilder, LoggingRuntime}
import slog4s.slf4j.MarkerStructureBuilder._
import slog4s.{LogEncoder, Logger, LoggerFactory, LoggingContext}

/** Slf4j backed [[slog4s.LoggerFactory]] instance. It's using logstash's
  * [[net.logstash.logback.marker.MapEntriesAppendingMarker]] to represent structured arguments.
  * It can optionally add a user-defined children Marker that might be used for advanced filtering.
  */
object Slf4jFactory {

  /** Builder pattern for [[slog4s.slf4j.Slf4jFactory]]. Bounds to a specific effect type.
    */
  def apply[F[_]: Sync]: Slf4jFactoryBuilder[F] =
    new Slf4jFactoryBuilder[F](Sync[F].pure(None), Map.empty)

  final class Slf4jFactoryBuilder[F[_]] private[slf4j] (
      markerF: F[Option[Marker]],
      extraArgs: Slf4jArgs
  )(implicit
      F: Sync[F]
  ) {

    def make(
        contextRuntime: ContextRuntime[F, Slf4jArgs]
    ): LoggingRuntime[F] = {
      import contextRuntime._
      new LoggingRuntime[F] {
        override def loggerFactory: LoggerFactory[F] =
          new LoggerFactory[F] {
            override def make(name: String): Logger[F] =
              new Slf4jLogger(
                org.slf4j.LoggerFactory.getLogger(name),
                as.get.map(extraArgs ++ _),
                markerF
              )(F)
          }
        override val loggingContext: LoggingContext[F] =
          Slf4jContext.make
      }
    }

    def makeFromBuilder(
        contextRuntimeBuilder: ContextRuntimeBuilder[F]
    ): F[LoggingRuntime[F]] = {
      contextRuntimeBuilder.make(Slf4jArgs.empty).map(make)
    }

    /** Add structured argument to be used by all log statements created by this [[slog4s.LoggerFactory]].
      * Typically this will be used for application version and similar universally applicable values.
      */
    def withArg[T: LogEncoder](
        key: String,
        value: T
    ): Slf4jFactoryBuilder[F] = {
      new Slf4jFactoryBuilder[F](
        markerF,
        extraArgs.updated(key, LogEncoder[T].encode(value))
      )
    }

    /** Use provided [[org.slf4j.Marker]] for each log message.
      */
    def withMarker(marker: F[Marker]): Slf4jFactoryBuilder[F] =
      withOptionalMarker(marker.map(Some(_)))

    /** Use provided optional [[org.slf4j.Marker]] for each log message.
      */
    def withOptionalMarker(
        marker: F[Option[Marker]]
    ): Slf4jFactoryBuilder[F] = {
      new Slf4jFactoryBuilder(marker, extraArgs)
    }

    def withoutContext: WithoutContextBuilder[F] =
      new WithoutContextBuilder(markerF, extraArgs)
  }

  final class WithoutContextBuilder[F[_]] private[slf4j] (
      markerF: F[Option[Marker]],
      extraArgs: Slf4jArgs
  )(implicit F: Sync[F]) {
    def loggerFactory: LoggerFactory[F] = {
      new LoggerFactory[F] {
        override def make(name: String): Logger[F] =
          new Slf4jLogger(
            org.slf4j.LoggerFactory.getLogger(name),
            F.pure(extraArgs),
            markerF
          )(F)
      }
    }
  }
}
