package slog

import cats.Contravariant
import cats.syntax.contravariant._
import slog.`export`.Exported

trait StructureEncoder[T] {
  def encode[O](value: T)(implicit structureBuilder: StructureBuilder[O]): O
}

object StructureEncoder extends StructureEncoderImplicits {
  def apply[T](implicit ev: StructureEncoder[T]): StructureEncoder[T] = ev

  implicit val structureEncoderContravariant: Contravariant[StructureEncoder] =
    new Contravariant[StructureEncoder] {
      override def contramap[A, B](
          fa: StructureEncoder[A]
      )(f: B => A): StructureEncoder[B] = new StructureEncoder[B] {
        override def encode[O](value: B)(
            implicit structureBuilder: StructureBuilder[O]
        ): O = fa.encode(f(value))
      }
    }

}

private[slog] trait StructureEncoderImplicits {
  implicit lazy val longEncoder: StructureEncoder[Long] =
    new StructureEncoder[Long] {
      override def encode[O](value: Long)(
          implicit structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.long(value)
    }

  implicit lazy val intEncoder: StructureEncoder[Int] =
    longEncoder.contramap(_.toLong)
  implicit lazy val shortEncoder: StructureEncoder[Short] =
    longEncoder.contramap(_.toLong)
  implicit lazy val byteEncoder: StructureEncoder[Byte] =
    longEncoder.contramap(_.toLong)

  implicit lazy val booleanEncoder: StructureEncoder[Boolean] = {
    new StructureEncoder[Boolean] {
      override def encode[O](value: Boolean)(
          implicit structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.boolean(value)
    }
  }

  implicit lazy val doubleEncoder: StructureEncoder[Double] =
    new StructureEncoder[Double] {
      override def encode[O](value: Double)(
          implicit structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.double(value)
    }

  implicit lazy val floatEncoder: StructureEncoder[Float] =
    doubleEncoder.contramap(_.toDouble)

  implicit lazy val stringEncoder: StructureEncoder[String] =
    new StructureEncoder[String] {
      override def encode[O](value: String)(
          implicit structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.string(value)
    }

  implicit def iterableEncoder[T: StructureEncoder, F[_]](
      implicit ev: F[T] <:< Iterable[T]
  ): StructureEncoder[F[T]] =
    new StructureEncoder[F[T]] {
      override def encode[O](
          value: F[T]
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        structureBuilder.array(value.map(StructureEncoder[T].encode[O]).toSeq)
      }
    }

  implicit def optionEncoder[T: StructureEncoder]: StructureEncoder[Option[T]] =
    new StructureEncoder[Option[T]] {
      override def encode[O](
          value: Option[T]
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        structureBuilder.option(value.map(StructureEncoder[T].encode(_)))
      }
    }

  implicit def tuple2Encoder[A, B](
      implicit ev1: StructureEncoder[A],
      ev2: StructureEncoder[B]
  ): StructureEncoder[(A, B)] = new StructureEncoder[(A, B)] {
    override def encode[O](value: (A, B))(
        implicit structureBuilder: StructureBuilder[O]
    ): O = {
      structureBuilder.array(List(ev1.encode(value._1), ev2.encode(value._2)))
    }
  }

  implicit def mapEncoder[K: StructureEncoder, V: StructureEncoder]
      : StructureEncoder[Map[K, V]] = new StructureEncoder[Map[K, V]] {
    override def encode[O](
        value: Map[K, V]
    )(implicit structureBuilder: StructureBuilder[O]): O = {
      structureBuilder.map(
        value.map(
          tuple =>
            StructureEncoder[K].encode(tuple._1) -> StructureEncoder[V]
              .encode(tuple._2)
        )
      )
    }
  }

  implicit def fromExported[T](
      implicit ev: Exported[StructureEncoder[T]]
  ): StructureEncoder[T] = ev.value
}
