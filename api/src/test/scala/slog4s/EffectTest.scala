package slog4s

import cats.effect.syntax.all._
import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.syntax.flatMap._
import org.scalactic.source
import org.scalatest.funspec.FixtureAnyFunSpec

import scala.concurrent.duration._

abstract class EffectTest[F[_]] extends FixtureAnyFunSpec {

  protected def asEffect(fixtureParam: FixtureParam): ConcurrentEffect[F]
  protected def asTimer(fixtureParam: FixtureParam): Timer[F]
  protected def timeout: FiniteDuration = 10.seconds

  def it_(
      specText: String
  )(f: FixtureParam => F[Unit])(implicit pos: source.Position): Unit = {
    it(specText) { fixture =>
      implicit val F = asEffect(fixture)
      implicit val T = asTimer(fixture)
      F.toIO(f(fixture).timeout(timeout))
        .unsafeRunSync()
    }
  }

  def info_(message: String)(f: F[Unit])(implicit F: Sync[F]): F[Unit] = {
    F.delay(info(message)) >> f
  }

  def validate(f: => Unit)(implicit F: Sync[F]): F[Unit] = {
    F.delay(f)
  }
}
