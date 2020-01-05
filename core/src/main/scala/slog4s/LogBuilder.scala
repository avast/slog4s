package slog4s

/**
  * A part of logging API for a specific logging level. This trait is used to build a list
  * of structured argument to be used for current logging statement.
  * @tparam F
  */
trait LogBuilder[F[_]] {

  /**
    * Log simple message with no exception.
    * @param msg message to be logged
    * @return
    */
  def log(msg: String): F[Unit]

  /**
    * Log simple message with an exception.
    * @param msg message to be logged
    * @param ex exception to be logged
    * @return
    */
  def log(ex: Throwable, msg: String): F[Unit]

  /**
    * Extends current logging statement with structured data.
    * @param key name of the argument
    * @param value value to be used as structured argument
    * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
    * @return a new [[LogBuilder]] instance containing provided structured argument
    */
  def withArg[T: LogEncoder](key: String, value: => T): LogBuilder[F]
}
