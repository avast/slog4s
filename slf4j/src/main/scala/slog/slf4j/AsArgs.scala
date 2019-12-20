package slog.slf4j

trait AsArgs[C] {
  def convert(c: C): Slf4jArgs
}

object AsArgs {
  implicit val identityInstance: AsArgs[Slf4jArgs] = v => v
}
