package slog4s.generic.internal

import magnolia.{CaseClass, SealedTrait}
import slog4s.{StructureBuilder, LogEncoder}

private[generic] object Common {
  def combine[T](
      caseClass: CaseClass[LogEncoder, T]
  ): LogEncoder[T] = {
    new LogEncoder[T] {
      override def encode[O](
          value: T
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        val params = caseClass.parameters.map { param =>
          param.label -> param.typeclass.encode(param.dereference(value))
        }
        structureBuilder.structure(caseClass.typeName.short, params.toMap)
      }
    }
  }
  def dispatch[T](
      sealedTrait: SealedTrait[LogEncoder, T]
  ): LogEncoder[T] = {
    new LogEncoder[T] {
      override def encode[O](
          value: T
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        sealedTrait.dispatch(value) { subtype =>
          subtype.typeclass.encode(subtype.cast(value))
        }
      }
    }
  }
}
