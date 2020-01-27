package slog4s.console

import cats.Applicative

trait ConsoleConfig[F[_]] {
  def level(name: String): F[Level]
}

object ConsoleConfig {
  def fixed[F[_]](
      fixedLevel: Level
  )(implicit F: Applicative[F]): ConsoleConfig[F] = new ConsoleConfig[F] {
    override def level(name: String): F[Level] = F.pure(fixedLevel)
  }
}
