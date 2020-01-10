package slog4s

/**
  * Typeclass used to modify a logging context. The logging context is a list
  * of named structured arguments that should be used by all applicable logging
  * statements, typically inside give scope.
  *
  * @tparam F
  */
trait LoggingContext[F[_]] {

  /**
    * Start defining a list of structured arguments with the first key/value pair.
    * @param key name of the argument
    * @param value value to be used as structured argument
    * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
    * @return a new instance of [[LoggingContext.LoggingBuilder]] containing provided structured argument
    */
  def withArg[T: LogEncoder](
      key: String,
      value: T
  ): LoggingContext.LoggingBuilder[F]
}

object LoggingContext {
  def apply[F[_]](implicit ev: LoggingContext[F]): LoggingContext[F] = ev

  /**
    * A list of named structured arguments that can be either extended or applied
    * to an effect.
    * @tparam F
    */
  trait LoggingBuilder[F[_]] {

    /**
      * Use all structured arguments in a given effect. All the logging statements
      * used inside the effect will be extended with these structured arguments.
      * @param fv
      * @tparam T
      * @return
      */
    def use[T](fv: F[T]): F[T]

    /**
      * Extend list of structured arguments.
      * @param key name of the argument
      * @param value value to be used as structured argument
      * @tparam T type of structured argument. It needs to implement [[LogEncoder]] typeclass.
      * @return a new instance of [[LoggingBuilder]] containing provided structured argument
      */
    def withArg[T: LogEncoder](key: String, value: T): LoggingBuilder[F]
  }
}
