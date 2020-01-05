package slog4s

import scala.reflect.ClassTag

/**
  * Our counterpart for http://www.slf4j.org/apidocs/org/slf4j/LoggerFactory.html.
  */
trait LoggerFactory[F[_]] {
  def make(name: String): Logger[F]

  def make[T](implicit classTag: ClassTag[T]): Logger[F] =
    make(classTag.runtimeClass.getName)
}
