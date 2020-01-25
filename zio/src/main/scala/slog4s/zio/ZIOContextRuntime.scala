package slog4s.zio

import slog4s.shared.{AsContext, ContextRuntime, UseContext}
import zio.{FiberRef, UIO, ZIO}

class ZIOContextRuntime[R, E, T] private (fiberRef: FiberRef[T])
    extends ContextRuntime[ZIO[R, E, *], T] {
  override implicit val as: AsContext[ZIO[R, E, *], T] =
    AsZIOContext.make(fiberRef)

  override implicit val use: UseContext[ZIO[R, E, *], T] =
    UseZIOContext.make(fiberRef)
}

object ZIOContextRuntime {
  def make[R, E, T](empty: T): UIO[ZIOContextRuntime[R, E, T]] = {
    FiberRef
      .make(empty, (first: T, _: T) => first)
      .map(new ZIOContextRuntime(_))
  }
}
