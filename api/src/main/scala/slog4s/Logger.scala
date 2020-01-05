package slog4s

import slog4s.macros.LoggerImpl

/**
  * Top level user-code logger API. There is a single goal of this class: provide logging
  * statement file/line location to various implementations of [[LocationAwareLogger]].
  */
class Logger[F[_]](val underlying: LocationAwareLogger[F]) {
  import scala.language.experimental.macros

  def debug: LevelLogBuilder[F] = macro LoggerImpl.debugImpl[F]
  def error: LevelLogBuilder[F] = macro LoggerImpl.errorImpl[F]
  def info: LevelLogBuilder[F] = macro LoggerImpl.infoImpl[F]
  def trace: LevelLogBuilder[F] = macro LoggerImpl.traceImpl[F]
  def warn: LevelLogBuilder[F] = macro LoggerImpl.warnImpl[F]
}
