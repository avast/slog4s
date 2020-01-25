package slog4s.monix

import monix.eval.{Task, TaskLocal}
import slog4s.shared.AsContext

object AsMonixContext {

  /**
    * Makes an [[slog4s.shared.UseContext]] based on [[monix.eval.TaskLocal]].
    * @param taskLocal
    */
  def identity[T](taskLocal: TaskLocal[T]): AsContext[Task, T] =
    make(taskLocal)(Predef.identity)

  /**
    * Makes an [[slog4s.shared.UseContext]] based on [[monix.eval.TaskLocal]]. As opposed to [[identity]] it allows
    * one to modify value using user defined function `map`.
    * @param taskLocal
    * @param map function used to modify value while reading the context from [[monix.eval.TaskLocal]]
    */
  def make[I, O](taskLocal: TaskLocal[I])(map: I => O): AsContext[Task, O] =
    new AsContext[Task, O] {
      override def get: Task[O] = taskLocal.read.map(map)
    }
}
