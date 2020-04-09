package slog4s.testkit.internal

import slog4s.StructureBuilder
import slog4s.testkit.Argument

private[testkit] object ArgumentStructureBuilder
    extends StructureBuilder[Argument] {

  override def boolean(value: Boolean): Argument = Argument.Boolean(value)
  override def long(value: Long): Argument = Argument.Number(value)
  override def double(value: Double): Argument = Argument.FloatNumber(value)
  override def string(value: String): Argument = Argument.String(value)
  override def structure(
      name: String,
      attributes: Map[String, Argument]
  ): Argument = Argument.Structure(name, attributes)
  override def option(value: Option[Argument]): Argument = Argument.Maybe(value)
  override def map(values: Map[String, Argument]): Argument =
    Argument.Map(values)
  override def array(values: Iterable[Argument]): Argument =
    Argument.List(values.toList)
}
