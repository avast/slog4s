package slog

import slog.macros.LoggerImpl

class Logger[F[_]](val underlying: LocationAwareLogger[F]) {
  import scala.language.experimental.macros

  def debug: LevelLogBuilder[F] = macro LoggerImpl.debugImpl[F]
  def error: LevelLogBuilder[F] = macro LoggerImpl.errorImpl[F]
  def info: LevelLogBuilder[F] = macro LoggerImpl.infoImpl[F]
  def trace: LevelLogBuilder[F] = macro LoggerImpl.traceImpl[F]
  def warn: LevelLogBuilder[F] = macro LoggerImpl.warnImpl[F]
}
