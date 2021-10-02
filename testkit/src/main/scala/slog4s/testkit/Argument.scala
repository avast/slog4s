package slog4s.testkit

import cats.Show
import cats.syntax.show._

sealed trait Argument

object Argument extends ArgumentImplicitInstances {
  final case class Number(value: Long) extends Argument
  final case class FloatNumber(value: Double) extends Argument
  final case class String(value: scala.Predef.String) extends Argument
  final case class Boolean(value: scala.Boolean) extends Argument
  final case class Maybe(value: Option[Argument]) extends Argument
  final case class List(value: scala.List[Argument]) extends Argument
  final case class Map(
      value: scala.collection.immutable.Map[scala.Predef.String, Argument]
  ) extends Argument
  final case class Structure(
      name: scala.Predef.String,
      value: scala.collection.immutable.Map[scala.Predef.String, Argument]
  ) extends Argument
}

protected[this] trait ArgumentImplicitInstances {
  implicit lazy val showInstance: Show[Argument] = Show {
    case Argument.Number(value)      => value.toString
    case Argument.FloatNumber(value) => value.toString
    case Argument.String(value)      => value.toString
    case Argument.Boolean(value)     => value.toString
    case Argument.Maybe(value)       => value.map(_.show).getOrElse("null")
    case Argument.List(value) => value.map(_.show).mkString("[", ", ", "]")
    case Argument.Map(value)  => showMap(value)
    case Argument.Structure(name, value) =>
      showMap(value.updated("type", Argument.String(name)))

  }

  private def showMap(value: Map[String, Argument]): String = {
    value
      .map { case (key, value) =>
        s"$key => ${value.show}"
      }
      .mkString("{", ", ", "}")
  }
}
