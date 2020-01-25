package slog4s.monix

import cats.kernel.Monoid
import monix.eval.{Task, TaskLocal}
import slog4s.shared.UseContext

object UseMonixContext {

  /**
    * Makes an [[slog4s.shared.UseContext]] based directly on an instance of [[monix.eval.TaskLocal]].
    */
  def identity[T: Monoid](taskLocal: TaskLocal[T]): UseContext[Task, T] =
    make(taskLocal)(Monoid[T].combine)

  /**
    * Makes an [[slog4s.shared.UseContext]] based on [[monix.eval.TaskLocal]]. As opposed to [[identity]] it allows
    * one to modify value  `set` function.
    * @param taskLocal
    * @param set function used to modify value before writing the context to [[monix.eval.TaskLocal]]
    */
  def make[I, O](taskLocal: TaskLocal[I])(
      set: (I, O) => I
  ): UseContext[Task, O] = new UseContext[Task, O] {

    override def use[V](value: O)(fv: Task[V]): Task[V] = {
      taskLocal.read.flatMap { input =>
        taskLocal.bind(set(input, value))(fv)
      }
    }
  }
}
