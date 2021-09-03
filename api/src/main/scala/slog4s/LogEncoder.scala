package slog4s

import cats.syntax.contravariant._
import cats.{Contravariant, Show}
import slog4s.`export`.Exported

/** Typeclass that is able to encode any value of T into logging friendly
  * format. The type of logging format is determined by provided instance of
  * [[StructureBuilder]].
  * @tparam T
  *   type to be encoded
  */
trait LogEncoder[T] {

  /** Encode value of T into desired value of type O that represents structured
    * logging value.
    * @param value
    *   to be encoded
    * @param structureBuilder
    *   instance of [[StructureBuilder]] typeclass used for building resulting
    *   value.
    * @tparam O
    *   type of target structured logging format
    * @return
    *   value encoded into target structured logging format
    */
  def encode[O](value: T)(implicit structureBuilder: StructureBuilder[O]): O
}

object LogEncoder extends HighPriorityLogEncoderImplicits {
  def apply[T](implicit ev: LogEncoder[T]): LogEncoder[T] = ev

  implicit val logEncoderContravariant: Contravariant[LogEncoder] =
    new Contravariant[LogEncoder] {
      override def contramap[A, B](
          fa: LogEncoder[A]
      )(f: B => A): LogEncoder[B] = new LogEncoder[B] {
        override def encode[O](value: B)(implicit
            structureBuilder: StructureBuilder[O]
        ): O = fa.encode(f(value))
      }
    }

}

private[slog4s] trait HighPriorityLogEncoderImplicits
    extends LowPriorityLogEncoderImplicits {

  implicit lazy val longEncoder: LogEncoder[Long] =
    new LogEncoder[Long] {
      override def encode[O](value: Long)(implicit
          structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.long(value)
    }

  implicit lazy val intEncoder: LogEncoder[Int] =
    longEncoder.contramap(_.toLong)
  implicit lazy val shortEncoder: LogEncoder[Short] =
    longEncoder.contramap(_.toLong)
  implicit lazy val byteEncoder: LogEncoder[Byte] =
    longEncoder.contramap(_.toLong)

  implicit lazy val booleanEncoder: LogEncoder[Boolean] = {
    new LogEncoder[Boolean] {
      override def encode[O](value: Boolean)(implicit
          structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.boolean(value)
    }
  }

  implicit lazy val doubleEncoder: LogEncoder[Double] =
    new LogEncoder[Double] {
      override def encode[O](value: Double)(implicit
          structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.double(value)
    }

  implicit lazy val floatEncoder: LogEncoder[Float] =
    doubleEncoder.contramap(_.toDouble)

  implicit lazy val stringEncoder: LogEncoder[String] =
    new LogEncoder[String] {
      override def encode[O](value: String)(implicit
          structureBuilder: StructureBuilder[O]
      ): O = structureBuilder.string(value)
    }

  implicit def iterableEncoder[T: LogEncoder, F[_]](implicit
      ev: F[T] <:< Iterable[T]
  ): LogEncoder[F[T]] =
    new LogEncoder[F[T]] {
      override def encode[O](
          value: F[T]
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        structureBuilder.array(value.map(LogEncoder[T].encode[O]))
      }
    }

  implicit def optionEncoder[T: LogEncoder]: LogEncoder[Option[T]] =
    new LogEncoder[Option[T]] {
      override def encode[O](
          value: Option[T]
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        structureBuilder.option(value.map(LogEncoder[T].encode(_)))
      }
    }

  implicit def tuple2Encoder[A, B](implicit
      ev1: LogEncoder[A],
      ev2: LogEncoder[B]
  ): LogEncoder[(A, B)] = new LogEncoder[(A, B)] {
    override def encode[O](value: (A, B))(implicit
        structureBuilder: StructureBuilder[O]
    ): O = {
      structureBuilder.array(List(ev1.encode(value._1), ev2.encode(value._2)))
    }
  }

  implicit def mapShowKeyEncoder[K: Show, V: LogEncoder]
      : LogEncoder[Map[K, V]] =
    new LogEncoder[Map[K, V]] {
      override def encode[O](
          value: Map[K, V]
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        structureBuilder.map(
          value.map(tuple =>
            Show[K].show(tuple._1) -> LogEncoder[V]
              .encode(tuple._2)
          )
        )
      }
    }

  implicit def fromExported[T](implicit
      ev: Exported[LogEncoder[T]]
  ): LogEncoder[T] = ev.value
}

private[slog4s] trait LowPriorityLogEncoderImplicits {
  implicit def genericMapEncoder[K: LogEncoder, V: LogEncoder]
      : LogEncoder[Map[K, V]] = {
    new LogEncoder[Map[K, V]] {
      private[this] val tupleEncoder = LogEncoder[(K, V)]
      override def encode[O](value: Map[K, V])(implicit
          structureBuilder: StructureBuilder[O]
      ): O = {
        val tuples = value.map(tupleEncoder.encode(_))
        structureBuilder.array(tuples)
      }
    }
  }
}
