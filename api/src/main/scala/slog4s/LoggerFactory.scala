package slog4s

import scala.reflect.ClassTag

trait LoggerFactory[F[_]] {
  def make(name: String): Logger[F]

  def make[T](implicit classTag: ClassTag[T]): Logger[F] =
    make(classTag.runtimeClass.getName)
}
