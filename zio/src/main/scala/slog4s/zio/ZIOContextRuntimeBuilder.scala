package slog4s.zio

import slog4s.shared.{ContextRuntime, ContextRuntimeBuilder}
import zio.ZIO

final class ZIOContextRuntimeBuilder[R, E]
    extends ContextRuntimeBuilder[ZIO[R, E, *]] {
  override def make[T](empty: T): ZIO[R, E, ContextRuntime[ZIO[R, E, *], T]] =
    ZIOContextRuntime.make(empty)
}

object ZIOContextRuntimeBuilder {
  val Task = new ZIOContextRuntimeBuilder[Any, Throwable]
}
