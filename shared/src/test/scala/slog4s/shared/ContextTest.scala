package slog4s.shared

import cats.effect.syntax.all._
import cats.effect.ConcurrentEffect
import cats.syntax.flatMap._
import cats.syntax.functor._
import slog4s.EffectTest
import slog4s.shared.ContextTest.Fixture
import cats.effect.{ Deferred, MonadCancel, Temporal }

abstract class ContextTest[F[_]](runtimeName: String) extends EffectTest[F] {
  override type FixtureParam = Fixture[F]
  override protected def asEffect(
      fixtureParam: Fixture[F]
  ): ConcurrentEffect[F] = fixtureParam.F
  override protected def asTimer(fixtureParam: Fixture[F]): Temporal[F] =
    fixtureParam.T

  describe(s"$runtimeName-based context") {
    it_("get context works") { fixture =>
      import fixture._
      useContext.use(1) {
        for {
          value <- asContext.get
          _ <- validate {
            assert(value === 1)
          }
        } yield ()
      }
    }
    it_("isolates changes") { fixture =>
      import fixture._
      useContext.use(1) {
        for {
          valueInner <- useContext.use(2)(asContext.get)
          valueOuter <- asContext.get
          _ <- validate {
            assert(valueInner === 2)
            assert(valueOuter === 1)
          }
        } yield ()
      }
    }
    it_("changes are not visible to a fiber") { fixture =>
      import fixture._
      useContext.use(1) {
        for {
          promise <- Deferred[F, Unit]
          fiber <- (promise.get >> asContext.get).start
          _ <- useContext.use(2) {
            for {
              _ <- promise.complete(())
              myValue <- asContext.get
              fiberValue <- fiber.join
              _ <- validate {
                assert(myValue === 2)
                assert(fiberValue === 1)
              }
            } yield ()
          }
        } yield ()
      }
    }
    it_("changes made by a fiber are not visible to the parent") { fixture =>
      import fixture._
      useContext.use(1) {
        for {
          promise <- Deferred[F, Unit]
          fiber <- (promise.get >> useContext.use(2)(
            asContext.get
          )).start
          _ <- useContext.use(3) {
            for {
              _ <- promise.complete(())
              myValueBefore <- asContext.get
              fiberValue <- fiber.join
              myValueAfter <- asContext.get
              _ <- validate {
                assert(myValueBefore === 3)
                assert(myValueAfter === 3)
                assert(fiberValue === 2)
              }
            } yield ()
          }
        } yield ()
      }
    }
    it_("interruption works") { fixture =>
      import fixture._
      def fiberCode(promise: Deferred[F, Unit]): F[Unit] =
        promise.complete(()) >> useContext.use(2)(F.never[Unit])
      useContext.use(1) {
        for {
          // promise used to indicate that finalizer inside `guarantee` was called.
          promise <- Deferred[F, Unit]
          // promise used to indicate that a fiber has entered inside `guarantee`.
          fiberStarted <- Deferred[F, Unit]
          fiber <- MonadCancel[F, Throwable]
            .guarantee(fiberCode(fiberStarted), promise.complete(()))
            .start
          _ <- fiberStarted.get
          _ <- fiber.cancel.start
          _ <- promise.get
        } yield ()
      }
    }
  }
}

object ContextTest {
  final class Fixture[F[_]](implicit
      val F: ConcurrentEffect[F],
      val T: Temporal[F],
      val asContext: AsContext[F, Int],
      val useContext: UseContext[F, Int]
  )
}
