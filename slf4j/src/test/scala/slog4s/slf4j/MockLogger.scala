package slog4s.slf4j

import cats.PartialOrder
import cats.syntax.partialOrder._
import org.slf4j.{Logger, Marker}
import slog4s.slf4j.MockLogger.{Level, Message}

import scala.collection.mutable.ListBuffer

class MockLogger extends Logger {
  override def getName: String = "Mock"

  var level: Level = Level.All
  var expectedMarker: Option[Marker] = None

  private def addTo(where: ListBuffer[Message], message: Message): Unit = {
    where.synchronized {
      where += message
    }
  }

  def reset(): Unit = {
    _traceMessages.clear()
    _debugMessages.clear()
    _infoMessages.clear()
    _warnMessages.clear()
    _errorMessages.clear()
  }

  private[this] val _traceMessages =
    collection.mutable.ListBuffer.empty[Message]
  def traceMessages: List[Message] = _traceMessages.toList

  override def isTraceEnabled: Boolean = level.value <= Level.Trace.value

  override def trace(msg: String): Unit = addTo(_traceMessages, Message(msg))

  override def trace(format: String, arg: Any): Unit =
    addTo(_traceMessages, Message(format, arg))

  override def trace(format: String, arg1: Any, arg2: Any): Unit =
    addTo(_traceMessages, Message(format, arg1, arg2))

  override def trace(format: String, arguments: AnyRef*): Unit =
    addTo(_traceMessages, Message(format, arguments))

  override def trace(msg: String, t: Throwable): Unit =
    addTo(_traceMessages, Message(msg, t))

  override def isTraceEnabled(marker: Marker): Boolean =
    expectedMarker.forall(_ == marker) && level <= Level.Trace

  override def trace(marker: Marker, msg: String): Unit =
    addTo(_traceMessages, Message(marker, msg))

  override def trace(marker: Marker, format: String, arg: Any): Unit =
    addTo(_traceMessages, Message(marker, format, arg))

  override def trace(
      marker: Marker,
      format: String,
      arg1: Any,
      arg2: Any
  ): Unit = addTo(_traceMessages, Message(marker, format, arg1, arg2))

  override def trace(marker: Marker, format: String, argArray: AnyRef*): Unit =
    addTo(_traceMessages, Message(marker, format, argArray))

  override def trace(marker: Marker, msg: String, t: Throwable): Unit =
    addTo(_traceMessages, Message(marker, msg, t))

  val _debugMessages = collection.mutable.ListBuffer.empty[Message]
  def debugMessages: List[Message] = _debugMessages.toList

  override def isDebugEnabled: Boolean = level <= Level.Debug

  override def debug(msg: String): Unit = addTo(_debugMessages, Message(msg))

  override def debug(format: String, arg: Any): Unit =
    addTo(_debugMessages, Message(format, arg))

  override def debug(format: String, arg1: Any, arg2: Any): Unit =
    addTo(_debugMessages, Message(format, arg1, arg2))

  override def debug(format: String, arguments: AnyRef*): Unit =
    addTo(_debugMessages, Message(format, arguments))

  override def debug(msg: String, t: Throwable): Unit =
    addTo(_debugMessages, Message(msg, t))

  override def isDebugEnabled(marker: Marker): Boolean =
    expectedMarker.forall(_ == marker) && level <= Level.Debug

  override def debug(marker: Marker, msg: String): Unit =
    addTo(_debugMessages, Message(marker, msg))

  override def debug(marker: Marker, format: String, arg: Any): Unit =
    addTo(_debugMessages, Message(marker, format, arg))

  override def debug(
      marker: Marker,
      format: String,
      arg1: Any,
      arg2: Any
  ): Unit = addTo(_debugMessages, Message(marker, format, arg1, arg2))

  override def debug(marker: Marker, format: String, argArray: AnyRef*): Unit =
    addTo(_debugMessages, Message(marker, format, argArray))

  override def debug(marker: Marker, msg: String, t: Throwable): Unit =
    addTo(_debugMessages, Message(marker, msg, t))

  val _infoMessages = collection.mutable.ListBuffer.empty[Message]
  def infoMessages: List[Message] = _infoMessages.toList

  override def isInfoEnabled: Boolean = level <= Level.Info

  override def info(msg: String): Unit = addTo(_infoMessages, Message(msg))

  override def info(format: String, arg: Any): Unit =
    addTo(_infoMessages, Message(format, arg))

  override def info(format: String, arg1: Any, arg2: Any): Unit =
    addTo(_infoMessages, Message(format, arg1, arg2))

  override def info(format: String, arguments: AnyRef*): Unit =
    addTo(_infoMessages, Message(format, arguments))

  override def info(msg: String, t: Throwable): Unit =
    addTo(_infoMessages, Message(msg, t))

  override def isInfoEnabled(marker: Marker): Boolean =
    expectedMarker.forall(_ == marker) && level <= Level.Info

  override def info(marker: Marker, msg: String): Unit =
    addTo(_infoMessages, Message(marker, msg))

  override def info(marker: Marker, format: String, arg: Any): Unit =
    addTo(_infoMessages, Message(marker, format, arg))

  override def info(
      marker: Marker,
      format: String,
      arg1: Any,
      arg2: Any
  ): Unit = addTo(_infoMessages, Message(marker, format, arg1, arg2))

  override def info(marker: Marker, format: String, argArray: AnyRef*): Unit =
    addTo(_infoMessages, Message(marker, format, argArray))

  override def info(marker: Marker, msg: String, t: Throwable): Unit =
    addTo(_infoMessages, Message(marker, msg, t))

  val _warnMessages = collection.mutable.ListBuffer.empty[Message]
  def warnMessages: List[Message] = _warnMessages.toList

  override def isWarnEnabled: Boolean = level <= Level.Warn

  override def warn(msg: String): Unit = addTo(_warnMessages, Message(msg))

  override def warn(format: String, arg: Any): Unit =
    addTo(_warnMessages, Message(format, arg))

  override def warn(format: String, arg1: Any, arg2: Any): Unit =
    addTo(_warnMessages, Message(format, arg1, arg2))

  override def warn(format: String, arguments: AnyRef*): Unit =
    addTo(_warnMessages, Message(format, arguments))

  override def warn(msg: String, t: Throwable): Unit =
    addTo(_warnMessages, Message(msg, t))

  override def isWarnEnabled(marker: Marker): Boolean =
    expectedMarker.forall(_ == marker) && level <= Level.Warn

  override def warn(marker: Marker, msg: String): Unit =
    addTo(_warnMessages, Message(marker, msg))

  override def warn(marker: Marker, format: String, arg: Any): Unit =
    addTo(_warnMessages, Message(marker, format, arg))

  override def warn(
      marker: Marker,
      format: String,
      arg1: Any,
      arg2: Any
  ): Unit = addTo(_warnMessages, Message(marker, format, arg1, arg2))

  override def warn(marker: Marker, format: String, argArray: AnyRef*): Unit =
    addTo(_warnMessages, Message(marker, format, argArray))

  override def warn(marker: Marker, msg: String, t: Throwable): Unit =
    addTo(_warnMessages, Message(marker, msg, t))

  val _errorMessages = collection.mutable.ListBuffer.empty[Message]
  def errorMessages: List[Message] = _errorMessages.toList

  override def isErrorEnabled: Boolean = level <= Level.Error

  override def error(msg: String): Unit = addTo(_errorMessages, Message(msg))

  override def error(format: String, arg: Any): Unit =
    addTo(_errorMessages, Message(format, arg))

  override def error(format: String, arg1: Any, arg2: Any): Unit =
    addTo(_errorMessages, Message(format, arg1, arg2))

  override def error(format: String, arguments: AnyRef*): Unit =
    addTo(_errorMessages, Message(format, arguments))

  override def error(msg: String, t: Throwable): Unit =
    addTo(_errorMessages, Message(msg, t))

  override def isErrorEnabled(marker: Marker): Boolean =
    expectedMarker.forall(_ == marker) && level <= Level.Error

  override def error(marker: Marker, msg: String): Unit =
    addTo(_errorMessages, Message(marker, msg))

  override def error(marker: Marker, format: String, arg: Any): Unit =
    addTo(_errorMessages, Message(marker, format, arg))

  override def error(
      marker: Marker,
      format: String,
      arg1: Any,
      arg2: Any
  ): Unit = addTo(_errorMessages, Message(marker, format, arg1, arg2))

  override def error(marker: Marker, format: String, argArray: AnyRef*): Unit =
    addTo(_errorMessages, Message(marker, format, argArray))

  override def error(marker: Marker, msg: String, t: Throwable): Unit =
    addTo(_errorMessages, Message(marker, msg, t))
}

object MockLogger {

  sealed abstract class Level(val value: Int)
  object Level {
    case object Off extends Level(Int.MaxValue)
    case object Trace extends Level(org.slf4j.event.Level.TRACE.toInt)
    case object Debug extends Level(org.slf4j.event.Level.DEBUG.toInt)
    case object Info extends Level(org.slf4j.event.Level.INFO.toInt)
    case object Warn extends Level(org.slf4j.event.Level.WARN.toInt)
    case object Error extends Level(org.slf4j.event.Level.ERROR.toInt)
    case object All extends Level(Int.MinValue)

    implicit val partialOrder: PartialOrder[Level] =
      PartialOrder.by[Level, Int](_.value)
  }

  final case class Message(
      marker: Option[Marker],
      msg: String,
      t: Option[Throwable],
      args: List[Any]
  )

  object Message {
    def apply(msg: String): Message = {
      Message(None, msg, None, Nil)
    }
    def apply(msg: String, t: Throwable): Message = {
      Message(None, msg, Option(t), Nil)
    }
    def apply(msg: String, arg: Any): Message = {
      Message(None, msg, None, List(arg))
    }
    def apply(msg: String, arg1: Any, arg2: Any): Message = {
      Message(None, msg, None, List(arg1, arg2))
    }
    def apply(msg: String, args: Seq[AnyRef]): Message = {
      Message(None, msg, None, args.toList)
    }
    def apply(marker: Marker, msg: String): Message = {
      Message(Option(marker), msg, None, Nil)
    }
    def apply(marker: Marker, msg: String, t: Throwable): Message = {
      Message(Option(marker), msg, Option(t), Nil)
    }
    def apply(marker: Marker, msg: String, arg: Any): Message = {
      Message(Option(marker), msg, None, List(arg))
    }
    def apply(marker: Marker, msg: String, arg1: Any, arg2: Any): Message = {
      Message(Option(marker), msg, None, List(arg1, arg2))
    }
    def apply(marker: Marker, msg: String, args: Seq[AnyRef]): Message = {
      Message(Option(marker), msg, None, args.toList)
    }
  }
}
