package slog4s.slf4j

import org.slf4j.Marker

/**
  * Typeclass that might extract [[Marker]] from any value of type C provided by the context.
  * This marker is then used for slf4j logging statement.
  * @tparam C
  */
trait AsMarker[C] {
  def extract(v: C): Option[Marker]
}
