package slog4s

import cats.Applicative

import scala.reflect.ClassTag

/**
  * Our counterpart for http://www.slf4j.org/apidocs/org/slf4j/LoggerFactory.html.
  */
trait LoggerFactory[F[_]] {
  def make(name: String): Logger[F]

  def make[T](implicit classTag: ClassTag[T]): Logger[F] =
    make(classTag.runtimeClass.getName)
}

object LoggerFactory {

  def noop[F[_]: Applicative]: LoggerFactory[F] = Function.const(Logger.noop[F])

}
