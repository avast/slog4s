package slog4s.monix

import monix.eval.{Task, TaskLocal}
import slog4s.shared.{AsContext, ContextRuntime, UseContext}

class MonixContextRuntime[T] private (taskLocal: TaskLocal[T])
    extends ContextRuntime[Task, T] {
  override implicit val as: AsContext[Task, T] =
    AsMonixContext.make(taskLocal)

  override implicit val use: UseContext[Task, T] =
    UseMonixContext.make(taskLocal)
}

object MonixContextRuntime {
  def make[T](empty: T): Task[MonixContextRuntime[T]] = {
    TaskLocal(empty).map(new MonixContextRuntime(_))
  }
}
