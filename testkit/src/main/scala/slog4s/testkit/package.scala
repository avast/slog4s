package slog4s

package object testkit {

  /** Type alias for representation of named log arguments.
    */
  type Arguments = Map[String, Argument]

  /** Type alias for a chain (list) of [[LogEvent]].
    */
  type LogEvents = List[LogEvent]
}
