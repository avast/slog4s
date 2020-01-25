package slog4s

import cats.kernel.Monoid

package object slf4j {

  /**
    * This is how our implementation actually expects contextual arguments.
    */
  type Slf4jArgs = Map[String, Any]
  object Slf4jArgs {
    val empty: Slf4jArgs = Map.empty
  }
  implicit val slf4jArgsMonoid: Monoid[Slf4jArgs] = new Monoid[Slf4jArgs] {
    override def empty: Slf4jArgs = Slf4jArgs.empty
    override def combine(x: Slf4jArgs, y: Slf4jArgs): Slf4jArgs = x ++ y
  }
}
