package slog4s.console

/** Text format that should be used.
  */
sealed trait Format
object Format {
  case object Plain extends Format
  case object Json extends Format
}
