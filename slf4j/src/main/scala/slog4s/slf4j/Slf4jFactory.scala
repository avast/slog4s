package slog4s.slf4j

import cats.effect.Sync
import cats.mtl.ApplicativeAsk
import org.slf4j.Marker
import slog4s.{LogEncoder, Logger, LoggerFactory}
import MarkerStructureBuilder._
import slog4s.shared.AsContext

/**
  * Slf4j backed [[slog4s.LoggerFactory]] instance. It's using logstash's
  * [[net.logstash.logback.marker.MapEntriesAppendingMarker]] to represent structured arguments.
  * It can optionally add a user-defined children Marker that might be used for advanced filtering.
  *
  */
object Slf4jFactory {

  /**
    * Builder pattern for [[slog4s.slf4j.Slf4jFactory]]. Bounds to a specific effect type.
    */
  def apply[F[_]: Sync]: Slf4jFactoryBuilder[F] = new Slf4jFactoryBuilder[F]

  final class Slf4jFactoryBuilder[F[_]]()(implicit F: Sync[F]) {

    /**
      * Extracts contextual structured arguments from an instance of [[slog4s.shared.AsContext]].
      */
    def useContext[C: AsArgs](
        implicit asContext: AsContext[F, C]
    ): Slf4jFactoryBuilderWithContext[F, C] = {
      new Slf4jFactoryBuilderWithContext[F, C](
        asContext.get,
        implicitly[AsArgs[C]].convert,
        Map.empty
      )
    }

    /**
      * Don't use any contextual structured arguments for this [[slog4s.LoggerFactory]].
      */
    def noContext: Slf4jFactoryBuilderWithContext[F, Unit] = {
      new Slf4jFactoryBuilderWithContext[F, Unit](
        F.unit,
        _ => Slf4jArgs.empty,
        Map.empty
      )
    }

  }

  final class Slf4jFactoryBuilderWithContext[F[_], C](
      ask: F[C],
      extractArgs: C => Slf4jArgs,
      defaultArgs: Slf4jArgs,
      extractMarker: C => Option[Marker] = (_: C) => None
  )(implicit F: Sync[F]) {

    /**
      * Defines how extra child [[org.slf4j.Marker]] should be extracted from context. Child marker
      * might be useful for advanced use cases, such as specific log message filtering.
      */
    def withMarker(
        implicit asMarker: AsMarker[C]
    ): Slf4jFactoryBuilderWithContext[F, C] = {
      new Slf4jFactoryBuilderWithContext(
        ask,
        extractArgs,
        defaultArgs,
        asMarker.extract
      )
    }

    /**
      * Add structured argument to be used by all log statements created by this [[slog4s.LoggerFactory]].
      * Typically this will be used for application version and similar universally applicable values.
      */
    def withArg[T: LogEncoder](
        key: String,
        value: T
    ): Slf4jFactoryBuilderWithContext[F, C] = {
      new Slf4jFactoryBuilderWithContext[F, C](
        ask,
        extractArgs,
        defaultArgs.updated(key, LogEncoder[T].encode(value)),
        extractMarker
      )
    }

    /**
      * Makes a new instance of [[slog4s.LoggerFactory]]. There should typically be only a single instance per
      * application.
      */
    def make: LoggerFactory[F] = new LoggerFactory[F] {
      override def make(name: String): Logger[F] =
        new Slf4jLogger[F, C](
          org.slf4j.LoggerFactory.getLogger(name),
          ask,
          extractArgs.andThen(_ ++ defaultArgs),
          extractMarker
        )
    }

  }

}
