package slog4s.slf4j

import slog4s.LoggingContext
import slog4s.shared.{MapLoggingContext, UseContext}
import slog4s.slf4j.MarkerStructureBuilder._

object Slf4jContext {

  /** Makes an instance of [[slog4s.LoggingContext]] that works well with [[Slf4jFactory]]. It is
    * backed by [[cats.mtl.ApplicativeLocal]].
    */
  def make[F[_]](implicit
      useContext: UseContext[F, Slf4jArgs]
  ): LoggingContext[F] = {
    new MapLoggingContext()
  }
}
