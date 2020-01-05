package slog4s

/**
  * Top level logging API for a specific logging level.
  * @tparam F
  */
trait LevelLogBuilder[F[_]] {

  /**
    * Log simple message with no exception nor message specific structured argument.
    * @param msg message to be logged
    * @return
    */
  def apply(msg: String): F[Unit]

  /**
    * Log simple message with an exception and no message specific structured argument.
    * @param msg message to be logged
    * @param ex exception to be logged
    * @return
    */
  def apply(ex: Throwable, msg: String): F[Unit]

  /**
    * Extends current logging statement with structured data.
    * @param key name of the argument
    * @param value value to be used as structured argument
    * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
    * @return a new [[LogBuilder]] instance containing provided structured argument
    */
  def withArg[T: LogEncoder](key: String, value: => T): LogBuilder[F]

  /**
    * Gets an instance of [[WhenEnabledLogBuilder]] associated with current logging level.
    * @return an instance of [[WhenEnabledLogBuilder]]
    */
  def whenEnabled: WhenEnabledLogBuilder[F]
}
