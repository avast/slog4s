package slog4s

/**
  * Describes log statement location (file, line...). Might not always be available
  * as it makes no sense in some context (typically when integrating with other logging
  * libraries).
  */
sealed trait Location

object Location {
  final case class Code(file: String, line: Int) extends Location
  case object NotUsed extends Location

  implicit def getLocation(
      implicit fileName: sourcecode.FileName,
      line: sourcecode.Line
  ): Location = Code(fileName.value, line.value)
}
