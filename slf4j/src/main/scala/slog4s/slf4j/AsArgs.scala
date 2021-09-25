package slog4s.slf4j

/** Typeclass that extract [[Slf4jArgs]] from any value of type C provided by
  * the context.
  *
  * @tparam C
  */
trait AsArgs[C] {
  def convert(c: C): Slf4jArgs
}

object AsArgs {
  implicit val identityInstance: AsArgs[Slf4jArgs] = v => v
}
