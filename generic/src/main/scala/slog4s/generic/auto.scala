package slog4s.generic

import magnolia.{CaseClass, SealedTrait}
import slog4s.LogEncoder
import slog4s.`export`.Exported
import slog4s.generic.internal.Common

import scala.language.experimental.macros

object auto {
  type Typeclass[T] = LogEncoder[T]
  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    Common.combine(caseClass)
  }
  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    Common.dispatch(sealedTrait)
  }

  /** Automatically derives an instance of [[slog4s.LogEncoder]] for case class
    * or sealed trait.
    */
  implicit def genLogEncoder[T]: Exported[LogEncoder[T]] =
    macro internal.Macros.exportEncoder[T]

}
