package slog4s.`export`

/** Simple wrapper around a value. Used for automatic typeclass derivation.
  */
case class Exported[T](value: T) extends AnyVal
