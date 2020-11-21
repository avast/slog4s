package slog4s

package object slf4j {

  /** This is how our implementation actually expects contextual arguments.
    */
  type Slf4jArgs = Map[String, Any]
  object Slf4jArgs {
    val empty: Slf4jArgs = Map.empty
  }
}
