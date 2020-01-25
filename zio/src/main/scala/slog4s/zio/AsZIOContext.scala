package slog4s.zio

import slog4s.shared.AsContext
import zio.{FiberRef, ZIO}

object AsZIOContext {
  def make[R, E, T](fiberRef: FiberRef[T]): AsContext[ZIO[R, E, *], T] =
    new AsContext[ZIO[R, E, *], T] {
      override def get: ZIO[R, E, T] = fiberRef.get
    }
}
