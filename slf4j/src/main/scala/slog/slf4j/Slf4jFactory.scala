package slog.slf4j

import cats.effect.Sync
import cats.mtl.ApplicativeAsk
import org.slf4j.Marker
import slog.{Logger, LoggerFactory, StructureEncoder}
import MarkerStructureBuilder._

object Slf4jFactory {

  def apply[F[_]: Sync]: Slf4jFactoryBuilder[F] = new Slf4jFactoryBuilder[F]

  final class Slf4jFactoryBuilder[F[_]]()(implicit F: Sync[F]) {
    def contextAsk[C: AsArgs](
        implicit ask: ApplicativeAsk[F, C]
    ): Slf4jFactoryBuilderWithContext[F, C] = {
      new Slf4jFactoryBuilderWithContext[F, C](
        ask.ask,
        implicitly[AsArgs[C]].convert,
        Map.empty
      )
    }

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

    def withArg[T: StructureEncoder](
        key: String,
        value: T
    ): Slf4jFactoryBuilderWithContext[F, C] = {
      new Slf4jFactoryBuilderWithContext[F, C](
        ask,
        extractArgs,
        defaultArgs.updated(key, StructureEncoder[T].encode(value)),
        extractMarker
      )
    }

    def make: LoggerFactory[F] = new LoggerFactory[F] {
      override def make(name: String): Logger[F] =
        new Logger[F](
          new Slf4jLogger[F, C](
            org.slf4j.LoggerFactory.getLogger(name),
            ask,
            extractArgs.andThen(_ ++ defaultArgs),
            extractMarker
          )
        )
    }

  }

}
