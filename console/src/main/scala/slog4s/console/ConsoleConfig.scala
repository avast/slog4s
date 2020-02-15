package slog4s.console

import cats.Applicative
import cats.effect.Sync
import slog4s.console.internal.StructuredConsoleConfig

trait ConsoleConfig[F[_]] {
  def level(name: String): F[Level]
}

object ConsoleConfig {
  def fixed[F[_]](
      fixedLevel: Level
  )(implicit F: Applicative[F]): ConsoleConfig[F] = new ConsoleConfig[F] {
    override def level(name: String): F[Level] = F.pure(fixedLevel)
  }

  def structured[F[_]: Sync](
      rootLevel: Level,
      levels: Map[String, Level]
  ): ConsoleConfig[F] = StructuredConsoleConfig.make(rootLevel, levels)
}
