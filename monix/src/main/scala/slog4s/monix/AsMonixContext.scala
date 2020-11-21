package slog4s.monix

import monix.eval.{Task, TaskLocal}
import slog4s.shared.AsContext

object AsMonixContext {

  /** Makes an [[slog4s.shared.UseContext]] based on [[monix.eval.TaskLocal]].
    * @param taskLocal
    */
  def make[T](taskLocal: TaskLocal[T]): AsContext[Task, T] =
    new AsContext[Task, T] {
      override def get: Task[T] = taskLocal.read
    }
}
