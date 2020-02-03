package slog4s.console

sealed trait Format

object Format {
  case object Plain extends Format
  case object Json extends Format
}
