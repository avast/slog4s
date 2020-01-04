package slog4s.generic.internal

import magnolia.Magnolia

import scala.reflect.macros.whitebox

private[generic] object Macros {
  def exportEncoder[T](
      c: whitebox.Context
  )(implicit ev: c.WeakTypeTag[T]): c.Tree = {
    import c.universe._
    val inner = Magnolia.gen(c)(ev)
    q"""
       _root_.slog4s.export.Exported($inner)
     """
  }
}
