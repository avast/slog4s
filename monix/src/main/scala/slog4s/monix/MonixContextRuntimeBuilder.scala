package slog4s.monix

import monix.eval.Task
import slog4s.shared.{ContextRuntime, ContextRuntimeBuilder}

object MonixContextRuntimeBuilder extends ContextRuntimeBuilder[Task] {

  override def make[T](empty: T): Task[ContextRuntime[Task, T]] =
    MonixContextRuntime.make(empty)
}
