package slog4s.console.internal

import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.order._
import slog4s._
import slog4s.console.ConsoleConfig
import slog4s.shared.AsContext

private[console] class ConsoleLogger[F[_]: Sync: Clock, T: StructureBuilder](
    name: String,
    consoleConfig: ConsoleConfig[F],
    formatter: Formatter[F, T]
)(implicit asContext: AsContext[F, Map[String, T]])
    extends Logger[F] {
  override val debug: LevelLogBuilder[F] = makeBuilder(Level.Debug)

  override val error: LevelLogBuilder[F] = makeBuilder(Level.Error)

  override val info: LevelLogBuilder[F] = makeBuilder(Level.Info)

  override val trace: LevelLogBuilder[F] = makeBuilder(Level.Trace)

  override val warn: LevelLogBuilder[F] = makeBuilder(Level.Warn)

  private def makeBuilder(level: Level): LevelLogBuilder[F] =
    new ConsoleBuilder(
      name,
      level,
      consoleConfig.level(name),
      formatter,
      Map.empty
    )

}

private class ConsoleBuilder[F[_], O](
    name: String,
    level: Level,
    getCurrentLevel: F[Level],
    formatter: Formatter[F, O],
    args: Map[String, O]
)(
    implicit F: Sync[F],
    C: Clock[F],
    O: StructureBuilder[O],
    asContext: AsContext[F, Map[String, O]]
) extends LevelLogBuilder[F]
    with LogBuilder[F] { self =>

  private def format(
      msg: String,
      ex: Option[Throwable],
      location: Location
  ): F[Unit] = {
    for {
      now <- C.realTime(TimeUnit.MILLISECONDS)
      contextArgs <- asContext.get
      _ <- F.suspend {
        val instant = Instant.ofEpochMilli(now)
        val threadName = Thread.currentThread().getName

        formatter
          .format(
            level,
            name,
            msg,
            ex,
            contextArgs ++ args,
            instant,
            threadName,
            location
          )
      }
    } yield ()
  }

  private def checkLevel(f: => F[Unit]): F[Unit] = {
    getCurrentLevel.flatMap { currentLevel =>
      if (currentLevel <= level) {
        f
      } else {
        F.unit
      }
    }
  }

  override def apply(msg: String)(implicit location: Location): F[Unit] =
    log(msg)
  override def apply(ex: Throwable, msg: String)(
      implicit location: Location
  ): F[Unit] = log(ex, msg)

  override val whenEnabled: WhenEnabledLogBuilder[F] =
    new WhenEnabledLogBuilder[F] {
      override def apply(f: LogBuilder[F] => F[Unit]): F[Unit] = {
        checkLevel(f(self))
      }
    }

  override def log(msg: String)(implicit location: Location): F[Unit] =
    checkLevel(format(msg, None, location))

  override def log(ex: Throwable, msg: String)(
      implicit location: Location
  ): F[Unit] = checkLevel(format(msg, Some(ex), location))

  override def withArg[T: LogEncoder](
      key: String,
      value: => T
  ): LogBuilder[F] = {
    new ConsoleBuilder(
      name,
      level,
      getCurrentLevel,
      formatter,
      args.updated(key, LogEncoder[T].encode(value))
    )
  }
}
