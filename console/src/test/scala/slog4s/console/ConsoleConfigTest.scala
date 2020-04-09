package slog4s.console

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.scalactic.source.Position
import org.scalatest.Outcome
import slog4s.{EffectTest, Level}
import slog4s.console.ConsoleConfigTest.Fixture

import scala.concurrent.ExecutionContext

class ConsoleConfigTest extends EffectTest[IO] {
  override protected def asEffect(
      fixtureParam: FixtureParam
  ): ConcurrentEffect[IO] = fixtureParam.F

  override protected def asTimer(fixtureParam: FixtureParam): Timer[IO] =
    fixtureParam.timer

  override protected def withFixture(test: OneArgTest): Outcome = {
    implicit val contextShift: ContextShift[IO] =
      IO.contextShift(ExecutionContext.global)
    implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
    test(new Fixture[IO]())
  }

  override type FixtureParam = Fixture[IO]

  describe("ConsoleConfig") {
    it_("structured config") { fixture =>
      import fixture._
      val levels = Map("a.b.c" -> Level.Debug, "a.b" -> Level.Trace)
      val consoleConfig = ConsoleConfig.structured(Level.Info, levels)
      def assertLevel(name: String, level: Level)(
          implicit position: Position
      ): IO[Unit] =
        validateLevel(consoleConfig.level(name))(level)
      for {
        _ <- info_("returns root level for unconfigured logger") {
          assertLevel("other", Level.Info)
        }
        _ <- info_("works for exact match") {
          assertLevel("a.b.c", Level.Debug) >>
            assertLevel("a.b", Level.Trace)
        }
        _ <- info_("works for child") {
          assertLevel("a.b.c.d", Level.Debug) >>
            assertLevel("a.b.x", Level.Trace)
        }
      } yield ()
    }
  }
}

object ConsoleConfigTest {
  import org.scalatest.matchers.should.Matchers._
  class Fixture[F[_]](
      implicit val F: ConcurrentEffect[F],
      val timer: Timer[F]
  ) {
    def validateLevel(
        fLevel: F[Level]
    )(expected: Level)(implicit position: Position): F[Unit] = {
      fLevel.flatMap(level => F.delay(level should equal(expected)))
    }
  }
}
