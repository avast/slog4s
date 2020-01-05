package slog4s.monix

import cats.Applicative
import cats.mtl.ApplicativeLocal
import monix.eval.{Task, TaskLocal}

/**
  * Monix specific implementations of [[ApplicativeLocal]] that can be used logging
  * context propagation. [[TaskLocal]] is used to store logging context.
  */
object MonixContext {

  /**
    * Makes an [[ApplicativeLocal]] based directly on an instance of [[TaskLocal]].
    */
  def identity[C](taskLocal: TaskLocal[C]): ApplicativeLocal[Task, C] =
    new ApplicativeLocal[Task, C] {
      override def local[A](f: C => C)(fa: Task[A]): Task[A] =
        taskLocal.read.flatMap { value =>
          taskLocal.bind(f(value))(fa)
        }

      override def scope[A](e: C)(fa: Task[A]): Task[A] = taskLocal.bind(e)(fa)

      override val applicative: Applicative[Task] = Applicative[Task]

      override def ask: Task[C] = taskLocal.read

      override def reader[A](f: C => A): Task[A] = ask.map(f)
    }

  /**
    * Makes an [[ApplicativeLocal]] based on [[TaskLocal]]. As opposed to [[identity]] it allows
    * one to modify value using a pair of functions `get` and `set`.
    * @param taskLocal
    * @param get function used to modify value while reading the context from [[TaskLocal]]
    * @param set function used to modify value before writing the context to [[TaskLocal]]
    * @tparam C type of the value in [[TaskLocal]]
    * @tparam T
    * @return
    */
  def make[C, T](taskLocal: TaskLocal[C])(get: C => T)(
      set: (T, C) => C
  ): ApplicativeLocal[Task, T] = new ApplicativeLocal[Task, T] {
    override def local[A](f: T => T)(fa: Task[A]): Task[A] = {
      taskLocal.read.flatMap { value =>
        taskLocal.bind(set(f(get(value)), value))(fa)
      }
    }

    override def scope[A](e: T)(fa: Task[A]): Task[A] =
      taskLocal.bindL(taskLocal.read.map(c => set(e, c)))(fa)

    override val applicative: Applicative[Task] = Applicative[Task]

    override def ask: Task[T] = taskLocal.read.map(get)

    override def reader[A](f: T => A): Task[A] = ask.map(f)
  }
}
