package slog4s.monix

import monix.eval.{Task, TaskLocal}
import slog4s.shared.UseContext

object UseMonixContext {

  /**
    * Makes an [[slog4s.shared.UseContext]] based on [[monix.eval.TaskLocal]].
    */
  def make[T](taskLocal: TaskLocal[T]): UseContext[Task, T] =
    new UseContext[Task, T] {

      override def update[V](f: T => T)(fv: Task[V]): Task[V] = {
        taskLocal.read.flatMap { old => taskLocal.bind(f(old))(fv) }
      }
    }
}
