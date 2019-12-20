package slog.slf4j

import org.slf4j.Marker

trait AsMarker[C] {
  def extract(v: C): Option[Marker]
}
