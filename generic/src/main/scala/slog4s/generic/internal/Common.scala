package slog4s.generic.internal

import magnolia.{CaseClass, SealedTrait}
import slog4s.{LogEncoder, StructureBuilder}

private final class CaseClassEncoder[T](caseClass: CaseClass[LogEncoder, T])
    extends LogEncoder[T] {
  override def encode[O](
      value: T
  )(implicit structureBuilder: StructureBuilder[O]): O = {
    if (caseClass.isObject) {
      structureBuilder.string(caseClass.typeName.short)
    } else {
      structureBuilder.map(encodeFields(value))
    }
  }

  def encodeSealedMember[O](
      value: T
  )(implicit structureBuilder: StructureBuilder[O]): O = {
    if (caseClass.isObject) {
      structureBuilder.string(caseClass.typeName.short)
    } else {
      structureBuilder.structure(
        caseClass.typeName.short,
        encodeFields(value)
      )
    }
  }

  private def encodeFields[O](
      value: T
  )(implicit structureBuilder: StructureBuilder[O]): Map[String, O] = {
    val params = caseClass.parameters.map { param =>
      param.label -> param.typeclass.encode(param.dereference(value))
    }
    params.toMap
  }
}

private[generic] object Common {
  def combine[T](
      caseClass: CaseClass[LogEncoder, T]
  ): LogEncoder[T] = new CaseClassEncoder[T](caseClass)

  def dispatch[T](
      sealedTrait: SealedTrait[LogEncoder, T]
  ): LogEncoder[T] = {
    new LogEncoder[T] {
      override def encode[O](
          value: T
      )(implicit structureBuilder: StructureBuilder[O]): O = {
        sealedTrait.dispatch(value) { subtype =>
          subtype.typeclass match {
            case caseClassEncoder: CaseClassEncoder[subtype.SType] =>
              caseClassEncoder.encodeSealedMember(subtype.cast(value))
            case other => other.encode(subtype.cast(value))
          }

        }
      }
    }
  }
}
