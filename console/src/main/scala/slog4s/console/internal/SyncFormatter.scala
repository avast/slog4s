package slog4s.console.internal

import java.time.Instant

import cats.effect.concurrent.Semaphore
import slog4s.Location
import slog4s.console.Level

private[console] class SyncFormatter[F[_], T](
    semaphore: Semaphore[F],
    underlying: Formatter[F, T]
) extends Formatter[F, T] {
  override def format(
      level: Level,
      logger: String,
      msg: String,
      throwable: Option[Throwable],
      args: Map[String, T],
      now: Instant,
      threadName: String,
      location: Location
  ): F[Unit] = semaphore.withPermit {
    underlying
      .format(level, logger, msg, throwable, args, now, threadName, location)
  }
}
