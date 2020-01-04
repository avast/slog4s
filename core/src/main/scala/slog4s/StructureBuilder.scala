package slog4s

trait StructureBuilder[T] {
  def boolean(value: Boolean): T
  def long(value: Long): T
  def double(value: Double): T
  def string(value: String): T
  def structure(name: String, attributes: Map[String, T]): T
  def option(value: Option[T]): T
  def map(values: Map[T, T]): T
  def array(values: Seq[T]): T
}

object StructureBuilder {
  def apply[T](implicit ev: StructureBuilder[T]): StructureBuilder[T] = ev
}
