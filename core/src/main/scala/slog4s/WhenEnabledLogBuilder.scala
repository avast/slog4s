package slog4s

import cats.FlatMap
import cats.syntax.flatMap._

/**
  * A part of logging API for a specific logging level. This trait is useful in case when
  * logging becomes expensive. It allows one to perform logging only when we need to, and
  * not do anything when logging for specific combination of logger and logging level is
  * turned off.
  * @tparam F
  */
trait WhenEnabledLogBuilder[F[_]] { self =>

  /**
    * Basic building block. If target combination of logger and logging level is
    * turned off, it return immediately, otherwise it executes provided callback.
    *
    * @param f callback to be used when logging is enabled
    * @return
    */
  def apply(f: LogBuilder[F] => F[Unit]): F[Unit]

  /**
    * Log simple message with no exception nor message specific structured argument.
    * @param msg message to be logged
    */
  def log(msg: String): F[Unit] = apply(_.log(msg))

  /**
    * Log simple message with an exception and no message specific structured argument.
    * @param msg message to be logged
    * @param ex exception to be logged
    */
  def log(ex: Throwable, msg: String): F[Unit] = apply(_.log(ex, msg))

  /**
    * Extends current logging statement with structured data. This is lazy variant that checks
    * if target logging level is enabled before actually doing anything.
    * @param key name of the argument
    * @param value value to be used as structured argument
    * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
    * @return a new [[WhenEnabledLogBuilder]] instance that will append structured argument lazily
    */
  def withArg[T: LogEncoder](
      key: String,
      value: => T
  ): WhenEnabledLogBuilder[F] = {
    new WhenEnabledLogBuilder[F] {
      override def apply(f: LogBuilder[F] => F[Unit]): F[Unit] = {
        self { logBuilder =>
          f(logBuilder.withArg(key, value))
        }
      }
    }
  }

  /**
    * Extends current logging statement with structured argument. This argument might be expensive to
    * compute, so it's wrapped inside an effect. The effect is only evaluated if need, which means only
    * when target logging level is enabled.
    * @param key name of the argument
    * @param fv value to be used as structured argument wrapped inside an effect
    * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
    * @return a new [[WhenEnabledLogBuilder]] instance that will append structured argument lazily
    */
  def computeArg[T: LogEncoder](key: String)(
      fv: F[T]
  )(implicit F: FlatMap[F]): WhenEnabledLogBuilder[F] = {
    new WhenEnabledLogBuilder[F] {
      override def apply(f: LogBuilder[F] => F[Unit]): F[Unit] = {
        self { logBuilder =>
          fv.flatMap { value =>
            f(logBuilder.withArg(key, value))
          }
        }
      }
    }
  }
}
