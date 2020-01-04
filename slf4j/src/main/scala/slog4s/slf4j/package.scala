package slog4s

package object slf4j {
  type Slf4jArgs = Map[String, Any]
  object Slf4jArgs {
    val empty: Slf4jArgs = Map.empty
  }
}
