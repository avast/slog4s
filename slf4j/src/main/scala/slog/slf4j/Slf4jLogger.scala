package slog.slf4j

import cats.effect.Sync
import cats.syntax.flatMap._
import net.logstash.logback.marker.MapEntriesAppendingMarker
import org.slf4j.Marker
import slog._
import slog.slf4j.MarkerStructureBuilder._

import scala.jdk.CollectionConverters._

private[slf4j] class Slf4jLogger[F[_], C](
    logger: org.slf4j.Logger,
    askContext: F[C],
    extractArgs: C => Slf4jArgs,
    extractMarker: C => Option[Marker]
)(
    implicit F: Sync[F]
) extends LocationAwareLogger[F] { self =>

  private type Args = Map[String, () => Any]
  private type DoLog = (Marker, String, Throwable) => Unit
  private type IsLogEnabled = Marker => Boolean

  private final class Level(
      doLog: DoLog,
      isLogEnabled: IsLogEnabled,
      args: Args
  ) extends LevelLogBuilder[F] {
    override def apply(msg: String): F[Unit] =
      self.log(doLog, isLogEnabled, args, msg, null)

    override def apply(ex: Throwable, msg: String): F[Unit] =
      self.log(doLog, isLogEnabled, args, msg, ex)

    override def withArg[T: StructureEncoder](
        key: String,
        value: => T
    ): LogBuilder[F] = {
      new Log(
        args.updated(key, () => StructureEncoder[T].encode(value)),
        doLog,
        isLogEnabled
      )
    }

    override def whenEnabled: WhenEnabledLogBuilder[F] =
      new WhenEnabled(isLogEnabled, new Log(args, doLog, isLogEnabled))
  }

  private final class Log(args: Args, doLog: DoLog, isLogEnabled: IsLogEnabled)
      extends LogBuilder[F] {
    override def log(msg: String): F[Unit] =
      self.log(doLog, isLogEnabled, args, msg, null)

    override def log(ex: Throwable, msg: String): F[Unit] =
      self.log(doLog, isLogEnabled, args, msg, ex)

    override def withArg[T: StructureEncoder](
        key: String,
        value: => T
    ): LogBuilder[F] = {
      new Log(
        args.updated(key, () => StructureEncoder[T].encode(value)),
        doLog,
        isLogEnabled
      )
    }
  }

  private final class WhenEnabled(
      isLogEnabled: IsLogEnabled,
      logBuilder: LogBuilder[F]
  ) extends WhenEnabledLogBuilder[F] {
    override def apply(f: LogBuilder[F] => F[Unit]): F[Unit] = {
      askContext.flatMap { context =>
        F.suspend {
          if (isLogEnabled(extractMarker(context).orNull)) {
            f(logBuilder)
          } else {
            F.unit
          }
        }
      }
    }
  }

  private def log(
      doLog: DoLog,
      isLogEnabled: IsLogEnabled,
      args: Args,
      msg: String,
      throwable: Throwable
  ): F[Unit] = {
    askContext.flatMap { context =>
      F.delay {
        val contextMarker = extractMarker(context)
        if (isLogEnabled(contextMarker.orNull)) {
          val allArgs = extractArgs(context) ++ args.map {
            case (key, value) => key -> value()
          }
          val marker = new MapEntriesAppendingMarker(allArgs.asJava)
          contextMarker.foreach(m => marker.add(m))
          doLog(
            marker,
            msg,
            throwable
          )
        }
      }
    }
  }

  private def makeInitialArgs(filename: String, line: Int): Args = {
    Map("x-file" -> (() => filename), "x-line" -> (() => line))
  }

  private[this] val doDebug: DoLog = logger.debug
  private[this] val isDebug: IsLogEnabled = logger.isDebugEnabled
  override def debug(filename: String, line: Int): LevelLogBuilder[F] =
    new Level(
      doDebug,
      isDebug,
      makeInitialArgs(filename, line)
    )

  private[this] val doError: DoLog = logger.error
  private[this] val isError: IsLogEnabled = logger.isErrorEnabled
  override def error(filename: String, line: Int): LevelLogBuilder[F] =
    new Level(
      doError,
      isError,
      makeInitialArgs(filename, line)
    )

  private[this] val doInfo: DoLog = logger.info
  private[this] val isInfo: IsLogEnabled = logger.isInfoEnabled
  override def info(filename: String, line: Int): LevelLogBuilder[F] =
    new Level(
      doInfo,
      isInfo,
      makeInitialArgs(filename, line)
    )

  private[this] val doTrace: DoLog = logger.trace
  private[this] val isTrace: IsLogEnabled = logger.isTraceEnabled
  override def trace(filename: String, line: Int): LevelLogBuilder[F] =
    new Level(
      doTrace,
      isTrace,
      makeInitialArgs(filename, line)
    )

  private[this] val doWarn: DoLog = logger.warn
  private[this] val isWarn: IsLogEnabled = logger.isWarnEnabled
  override def warn(filename: String, line: Int): LevelLogBuilder[F] =
    new Level(
      doWarn,
      isWarn,
      makeInitialArgs(filename, line)
    )
}
