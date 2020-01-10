package slog4s

sealed trait Location

object Location {
  final case class Code(file: String, line: Int) extends Location
  case object NotUsed extends Location

  implicit def getLocation(
      implicit fileName: sourcecode.FileName,
      line: sourcecode.Line
  ): Location = Code(fileName.value, line.value)
}
