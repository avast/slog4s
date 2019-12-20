package slog.generic

import magnolia.{CaseClass, Magnolia, SealedTrait}
import slog.StructureEncoder
import slog.generic.internal.Common
import scala.language.experimental.macros

object semi {
  type Typeclass[T] = StructureEncoder[T]
  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    Common.combine(caseClass)
  }
  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    Common.dispatch(sealedTrait)
  }
  def structureEncoder[T]: StructureEncoder[T] = macro Magnolia.gen[T]
}
