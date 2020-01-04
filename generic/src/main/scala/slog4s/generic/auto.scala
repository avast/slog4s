package slog4s.generic

import magnolia.{CaseClass, SealedTrait}
import slog4s.StructureEncoder
import slog4s.`export`.Exported
import slog4s.generic.internal.Common

import scala.language.experimental.macros

object auto {
  type Typeclass[T] = StructureEncoder[T]
  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    Common.combine(caseClass)
  }
  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    Common.dispatch(sealedTrait)
  }

  implicit def genStructureEncoder[T]: Exported[StructureEncoder[T]] =
    macro internal.Macros.exportEncoder[T]

}
