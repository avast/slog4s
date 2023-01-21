package slog4s.generic

import magnolia.{CaseClass, Magnolia, SealedTrait}
import slog4s.LogEncoder
import slog4s.generic.internal.Common
import scala.language.experimental.macros

object semi {
  type Typeclass[T] = LogEncoder[T]
  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    Common.combine(caseClass)
  }
  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    Common.dispatch(sealedTrait)
  }

  /** Semi automatic derivation of [[LogEncoder]] instance for case class or
    * sealed trait.
    */
  def logEncoder[T]: LogEncoder[T] = macro Magnolia.gen[T]
}
