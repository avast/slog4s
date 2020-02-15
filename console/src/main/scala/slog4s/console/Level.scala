package slog4s.console

import cats.Order
import cats.instances.int._

/**
  * Describes a log severity level.
  */
sealed abstract class Level(private[console] val value: Int)
object Level {
  case object Trace extends Level(10)
  case object Debug extends Level(20)
  case object Info extends Level(30)
  case object Warn extends Level(40)
  case object Error extends Level(50)

  implicit val order: Order[Level] = Order.by[Level, Int](_.value)
}
