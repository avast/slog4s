package slog4s

import cats.Order

/**
  * Describes a log severity level.
  */
sealed abstract class Level(private[slog4s] val value: Int)
object Level {
  case object Trace extends Level(10)
  case object Debug extends Level(20)
  case object Info extends Level(30)
  case object Warn extends Level(40)
  case object Error extends Level(50)

  implicit val order: Order[Level] = Order.by[Level, Int](_.value)
}
