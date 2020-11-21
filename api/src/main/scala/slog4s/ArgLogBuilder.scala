package slog4s

/** A part of logging API for a specific logging level. This trait is used to build a list
  * of structured argument to be used for current logging statement.
  *
  * @tparam F
  */
trait ArgLogBuilder[F[_]] {

  /** Log simple message with no exception.
    * @param msg message to be logged
    * @return
    */
  def log(msg: => String)(implicit location: Location): F[Unit]

  /** Log simple message with an exception.
    * @param msg message to be logged
    * @param ex exception to be logged
    * @return
    */
  def log(ex: Throwable, msg: => String)(implicit location: Location): F[Unit]

  /** Extends current logging statement with structured data.
    *
    * @param key name of the argument
    * @param value value to be used as structured argument
    * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
    * @return a new [[ArgLogBuilder]] instance containing provided structured argument
    */
  def withArg[T: LogEncoder](key: String, value: => T): ArgLogBuilder[F]
}

private[slog4s] class DeferredArgLogBuilder[F[_]](
    whenEnabledLogBuilder: WhenEnabledLogBuilder[F]
) extends ArgLogBuilder[F] {
  override def log(msg: => String)(implicit location: Location): F[Unit] =
    whenEnabledLogBuilder(_.log(msg))

  override def log(ex: Throwable, msg: => String)(implicit
      location: Location
  ): F[Unit] = whenEnabledLogBuilder(_.log(ex, msg))

  override def withArg[T: LogEncoder](
      key: String,
      value: => T
  ): ArgLogBuilder[F] =
    new DeferredArgLogBuilder(whenEnabledLogBuilder.withArg(key, value))
}
