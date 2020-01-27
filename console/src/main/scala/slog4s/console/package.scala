package slog4s

package object console {

  /**
    * This is how our implementation actually expects contextual arguments.
    */
  type PlainArgs = Map[String, String]
  object PlainArgs {
    val empty: PlainArgs = Map.empty
  }
}
