package slog4s.monix

import cats.Applicative
import cats.mtl.ApplicativeLocal
import monix.eval.{Task, TaskLocal}

object MonixContext {

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
