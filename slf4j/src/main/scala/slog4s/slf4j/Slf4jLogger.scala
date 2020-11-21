package slog4s.slf4j

import cats.effect.Sync
import cats.syntax.flatMap._
import net.logstash.logback.marker.MapEntriesAppendingMarker
import org.slf4j.Marker
import slog4s._
import slog4s.slf4j.MarkerStructureBuilder._

import scala.jdk.CollectionConverters._

private[slf4j] class Slf4jLogger[F[_]](
    logger: org.slf4j.Logger,
    askContext: F[Slf4jArgs],
    extractMarker: F[Option[Marker]]
)(implicit
    F: Sync[F]
) extends Logger[F] { self =>

  private type Args = Map[String, () => Any]
  private type DoLog = (Marker, String, Throwable) => Unit
  private type IsLogEnabled = Marker => Boolean

  private final class Level(
      doLog: DoLog,
      isLogEnabled: IsLogEnabled
  ) extends LevelLogBuilder[F] {
    override def whenEnabled: WhenEnabledLogBuilder[F] =
      new WhenEnabled(isLogEnabled, new Log(Map.empty, doLog, isLogEnabled))
  }

  private final class Log(args: Args, doLog: DoLog, isLogEnabled: IsLogEnabled)
      extends LogBuilder[F] {
    override def log(msg: String)(implicit location: Location): F[Unit] =
      self.log(doLog, isLogEnabled, args, msg, null, location)

    override def log(ex: Throwable, msg: String)(implicit
        location: Location
    ): F[Unit] =
      self.log(doLog, isLogEnabled, args, msg, ex, location)

    override def withArg[T: LogEncoder](
        key: String,
        value: => T
    ): LogBuilder[F] = {
      new Log(
        args.updated(key, () => LogEncoder[T].encode(value)),
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
      extractMarker.flatMap { maybeMarker =>
        F.suspend {
          if (isLogEnabled(maybeMarker.orNull)) {
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
      throwable: Throwable,
      location: Location
  ): F[Unit] = {
    extractMarker.flatMap { maybeMarker =>
      F.delay {
        if (isLogEnabled(maybeMarker.orNull)) {
          askContext.flatMap { context =>
            val allArgs = makeLocationArgs(location) ++ context ++ args
              .map { case (key, value) =>
                key -> value()
              }
            val marker = new MapEntriesAppendingMarker(allArgs.asJava)
            maybeMarker.foreach(m => marker.add(m))
            F.delay {
              doLog(
                marker,
                msg,
                throwable
              )
            }
          }
        } else {
          F.unit
        }
      }.flatten
    }
  }

  private def makeLocationArgs(location: Location): Slf4jArgs = {
    location match {
      case Location.Code(file, line) =>
        Map(
          Slf4jLogger.FileKey -> file,
          Slf4jLogger.LineKey -> line
        )
      case Location.NotUsed =>
        Map.empty
    }
  }

  private[this] val doDebug: DoLog = logger.debug
  private[this] val isDebug: IsLogEnabled = logger.isDebugEnabled
  override def debug: LevelLogBuilder[F] =
    new Level(
      doDebug,
      isDebug
    )

  private[this] val doError: DoLog = logger.error
  private[this] val isError: IsLogEnabled = logger.isErrorEnabled
  override def error: LevelLogBuilder[F] =
    new Level(
      doError,
      isError
    )

  private[this] val doInfo: DoLog = logger.info
  private[this] val isInfo: IsLogEnabled = logger.isInfoEnabled
  override def info: LevelLogBuilder[F] =
    new Level(
      doInfo,
      isInfo
    )

  private[this] val doTrace: DoLog = logger.trace
  private[this] val isTrace: IsLogEnabled = logger.isTraceEnabled
  override def trace: LevelLogBuilder[F] =
    new Level(
      doTrace,
      isTrace
    )

  private[this] val doWarn: DoLog = logger.warn
  private[this] val isWarn: IsLogEnabled = logger.isWarnEnabled
  override def warn: LevelLogBuilder[F] =
    new Level(
      doWarn,
      isWarn
    )
}

private[slf4j] object Slf4jLogger {
  val FileKey = "file"
  val LineKey = "line"
}
