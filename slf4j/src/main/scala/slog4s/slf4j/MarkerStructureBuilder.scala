package slog4s.slf4j

import slog4s.StructureBuilder

import scala.jdk.CollectionConverters._

private[slf4j] object MarkerStructureBuilder {
  implicit val markerStructureBuilder: StructureBuilder[Any] =
    new StructureBuilder[Any] {

      override def boolean(value: Boolean): Any = value

      override def long(value: Long): Any = value

      override def double(value: Double): Any = value

      override def string(value: String): Any = value

      override def structure(name: String, attributes: Map[String, Any]): Any =
        attributes.asJava

      override def option(value: Option[Any]): Any = value.orNull

      override def map(values: Map[String, Any]): Any = values.asJava

      override def array(values: Iterable[Any]): Any = values.asJava
    }
}
