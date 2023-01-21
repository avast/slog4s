package slog4s

/** Typeclass used to convert well defined values into desired structured
  * logging format. Typically this will be some sort of recursive data structure
  * similar to JSON.
  * @tparam T
  */
trait StructureBuilder[T] {

  /** Converts [[scala.Boolean]] value into desired type.
    */
  def boolean(value: Boolean): T

  /** Converts [[scala.Long]] value into desired type.
    */
  def long(value: Long): T

  /** Converts [[scala.Double]] value into desired type.
    */
  def double(value: Double): T

  /** Converts [[java.lang.String]] value into desired type.
    */
  def string(value: String): T

  /** Converts structure-like value into desired type.
    * @param name
    *   name of the structure. Typically a class name.
    * @param attributes
    *   map of field name and its value
    */
  def structure(name: String, attributes: Map[String, T]): T

  /** Converts [[scala.Option]] value into desired type.
    */
  def option(value: Option[T]): T

  /** Converts a [[scala.collection.Map]] of [[java.lang.String]] keys into
    * desired type.
    */
  def map(values: Map[String, T]): T

  /** Converts sequence of values into desired type.
    */
  def array(values: Iterable[T]): T
}

object StructureBuilder {
  def apply[T](implicit ev: StructureBuilder[T]): StructureBuilder[T] = ev
}
