package slog4s

import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.concurrent.duration.Duration

class MockClock[F[_]](val real: cats/effect/Ref[F, Long], val mono: cats/effect/Ref[F, Long])(implicit
    F: Monad[F]
) extends Clock[F] {
  def moveForward(duration: Duration): F[Unit] = {
    val value = duration.toMillis
    real.update(_ + value) >> mono.update(_ + value).void
  }

  override def realTime(unit: TimeUnit): F[Long] =
    real.get.map(Duration(_, TimeUnit.MILLISECONDS).toUnit(unit).toLong)
  override def monotonic(unit: TimeUnit): F[Long] =
    mono.get.map(Duration(_, TimeUnit.MILLISECONDS).toUnit(unit).toLong)
}

object MockClock {
  def make[F[_]: Sync]: F[MockClock[F]] = {
    for {
      mono <- cats/effect/Ref.of(0L)
      real <- cats/effect/Ref.of(0L)
    } yield new MockClock(mono, real)
  }
}
