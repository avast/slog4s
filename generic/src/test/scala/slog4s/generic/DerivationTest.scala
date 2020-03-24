package slog4s.generic

import cats.Show
import org.scalatest.funspec.AnyFunSpec
import slog4s.{LogEncoder, StructureBuilder}

class DerivationTest extends AnyFunSpec {
  import auto._
  import cats.instances.all._

  describe("LogEncoder") {
    describe("is derived for") {
      it("Int") {
        test[Int](42, 42)
      }
      it("Long") {
        test[Long](42L, 42L)
      }
      it("Byte") {
        test[Byte](42, 42)
      }
      it("Short") {
        test[Short](42, 42)
      }
      it("Boolean") {
        test(true, true)
      }
      it("String") {
        test("test", "test")
      }
      it("Float") {
        test[Float](42.0f, 42.0f)
      }
      it("Double") {
        test[Double](42.0, 42.0)
      }
      it("List[_]") {
        test[List[Int]](List(1, 2, 3), List(1, 2, 3))
      }
      it("Seq[_]") {
        test(Seq(1, 2, 3), Seq(1, 2, 3))
      }
      it("Option[_] - Some") {
        test(Option(42), 42)
      }
      it("Option[_] - None") {
        test[Option[Int]](None, null)
      }
      it("Set[_]") {
        test(Set(42), Set(42))
      }
      it("Map[String, _]") {
        test(Map("key" -> "value"), Map("key" -> "value"))
      }
      it("Map[Int, _]") {
        test(Map(42 -> "value"), Map("42" -> "value"))
      }
      it("Map[_:Show, _]") {
        class Key
        implicit val keyShow: Show[Key] = _ => "<key>"
        test(Map(new Key -> "value"), Map("<key>" -> "value"))
      }
      it("Map[_, _]") {
        case class Key(value: String)
        val expected: List[List[Any]] =
          List(List(Map("value" -> "<key>"), "value"))
        test(
          Map(Key("<key>") -> "value"),
          expected
        )
      }
      it("case class") {
        case class Tmp(value: List[Int])
        test(Tmp(List(42)), Map("value" -> List(42)))
      }
      it("sealed trait") {
        sealed trait Tmp
        object Tmp {
          case class Value(value: Int) extends Tmp
        }
        test(Tmp.Value(42), Map("value" -> 42))
      }
      it("sealed trait (case object)") {
        sealed trait Foo
        case object Bar extends Foo
        case object Baz extends Foo
        test(Bar, "Bar")
        test(Baz, "Baz")
      }
    }
  }

  def test[T: LogEncoder](value: T, expected: Any): Unit = {
    val result = LogEncoder[T].encode(value)
    assert(result == expected)
  }

  implicit val anyBuilder: StructureBuilder[Any] = new StructureBuilder[Any] {

    override def boolean(value: Boolean): Any = value

    override def long(value: Long): Any = value

    override def double(value: Double): Any = value

    override def string(value: String): Any = value

    override def structure(name: String, attributes: Map[String, Any]): Any =
      attributes

    override def option(value: Option[Any]): Any = value.orNull

    override def map(values: Map[String, Any]): Any = values

    override def array(values: Iterable[Any]): Any = values
  }

}
