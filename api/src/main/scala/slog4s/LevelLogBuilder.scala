package slog4s

import cats.Applicative

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
  def apply(msg: => String)(implicit location: Location): F[Unit] =
    whenEnabled(_.log(msg))

  /**
    * Log simple message with an exception and no message specific structured argument.
    * @param msg message to be logged
    * @param ex exception to be logged
    * @return
    */
  def apply(ex: Throwable, msg: => String)(
      implicit location: Location
  ): F[Unit] =
    whenEnabled(_.log(ex, msg))

  /**
    * Extends current logging statement with structured data.
    * @param key name of the argument
    * @param value value to be used as structured argument
    * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
    * @return a new [[ArgLogBuilder]] instance containing provided structured argument
    */
  def withArg[T: LogEncoder](key: String, value: => T): ArgLogBuilder[F] =
    new DeferredArgLogBuilder[F](whenEnabled.withArg(key, value))

  /**
    * Gets an instance of [[WhenEnabledLogBuilder]] associated with current logging level.
    * @return an instance of [[WhenEnabledLogBuilder]]
    */
  def whenEnabled: WhenEnabledLogBuilder[F]
}

object LevelLogBuilder {
  def noop[F[_]](implicit F: Applicative[F]): LevelLogBuilder[F] =
    new LevelLogBuilder[F] {
      override val whenEnabled: WhenEnabledLogBuilder[F] =
        WhenEnabledLogBuilder.noop
    }
}
