package slog4s.zio

import slog4s.shared.UseContext
import zio.{FiberRef, ZIO}

object UseZIOContext {
  def make[R, E, T](fiberRef: FiberRef[T]): UseContext[ZIO[R, E, *], T] =
    new UseContext[ZIO[R, E, *], T] {

      override def update[V](f: T => T)(fv: ZIO[R, E, V]): ZIO[R, E, V] = {
        fiberRef.get.flatMap { old => fiberRef.locally(f(old))(fv) }
      }
    }
}
