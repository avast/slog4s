package slog4s.macros

import java.io.File

import slog4s.LevelLogBuilder

import scala.reflect.macros.blackbox

private[slog4s] object LoggerImpl {
  def infoImpl[F[_]](c: blackbox.Context): c.Expr[LevelLogBuilder[F]] = {
    impl[F](c)(c.universe.TermName("info"))
  }

  def debugImpl[F[_]](c: blackbox.Context): c.Expr[LevelLogBuilder[F]] = {
    impl[F](c)(c.universe.TermName("debug"))
  }

  def traceImpl[F[_]](c: blackbox.Context): c.Expr[LevelLogBuilder[F]] = {
    impl[F](c)(c.universe.TermName("trace"))
  }

  def warnImpl[F[_]](c: blackbox.Context): c.Expr[LevelLogBuilder[F]] = {
    impl[F](c)(c.universe.TermName("warn"))
  }

  def errorImpl[F[_]](c: blackbox.Context): c.Expr[LevelLogBuilder[F]] = {
    impl[F](c)(c.universe.TermName("error"))
  }

  private def impl[F[_]](
      c: blackbox.Context
  )(level: c.universe.TermName): c.Expr[LevelLogBuilder[F]] = {
    import c.universe._
    val filename =
      c.enclosingPosition.source.path.split(File.separatorChar).last
    val file = Literal(Constant(filename))
    val line = Literal(Constant(c.enclosingPosition.line))
    val tree = q"${c.prefix}.underlying.$level($file, $line)"
    c.Expr(tree)
  }
}
