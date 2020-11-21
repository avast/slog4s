package slog4s.console

import cats.Applicative
import cats.effect.Sync
import slog4s.Level
import slog4s.console.internal.StructuredConsoleConfig

/** Simple method used to determine active log level for particular logger.
  */
trait ConsoleConfig[F[_]] {
  def level(name: String): F[Level]
}

object ConsoleConfig {

  /** Creates a [[ConsoleConfig]] that returns given log level for all loggers.
    */
  def fixed[F[_]](
      fixedLevel: Level
  )(implicit F: Applicative[F]): ConsoleConfig[F] = new ConsoleConfig[F] {
    override def level(name: String): F[Level] = F.pure(fixedLevel)
  }

  /** Creates a [[ConsoleConfig]] that is based on a concept of parent logger.
    * Each logger level can be configured individually. If not configured, parent logger
    * configuration will be used. If it is not configured as well, we are searching all
    * the way up until we reach "root" logger.
    */
  def structured[F[_]: Sync](
      rootLevel: Level,
      levels: Map[String, Level]
  ): ConsoleConfig[F] = StructuredConsoleConfig.make(rootLevel, levels)
}
