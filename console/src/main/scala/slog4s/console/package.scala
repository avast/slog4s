package slog4s

import io.circe.Json

package object console {

  /** This is how our implementation actually expects contextual arguments for
    * plain format.
    */
  type PlainArgs = Map[String, String]
  object PlainArgs {
    val empty: PlainArgs = Map.empty
  }

  /** This is how our implementation actually expects contextual arguments for
    * JSON format.
    */
  type JsonArgs = Map[String, Json]
  object JsonArgs {
    val empty: JsonArgs = Map.empty
  }
}
