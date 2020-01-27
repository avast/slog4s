package slog4s.console

import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.concurrent.{Executors, ThreadFactory, TimeUnit}

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Clock, ConcurrentEffect, IO, Resource, Sync, Timer}
import cats.syntax.all._
import slog4s.console.ConsoleLoggerTest.{Fixture, MockClock, Output}
import slog4s.shared.{
  AsContext,
  ContextRuntime,
  ContextRuntimeBuilder,
  UseContext
}
import slog4s.{EffectTest, Location, LoggerFactory, LoggingContext}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, TimeUnit}

abstract class ConsoleLoggerTest[F[_]](format: Format) extends EffectTest[F] {
  override protected def asEffect(
      fixtureParam: FixtureParam
  ): ConcurrentEffect[F] = fixtureParam.F

  override protected def asTimer(fixtureParam: FixtureParam): Timer[F] =
    fixtureParam.T

  override type FixtureParam = ConsoleLoggerTest.Fixture[F]

  val testMessage = "test message"
  val testFile = "TestFile.scala"
  val testLine = 42
  implicit val location = Location.Code(testFile, testLine)
  val boom = new Exception("boom!")
  val threadName = "test-thread"

  protected def makeIOFixture: Resource[IO, Fixture[IO]] = {
    def makeExecutor =
      Executors.newSingleThreadExecutor(new ThreadFactory {
        override def newThread(r: Runnable): Thread = {
          new Thread(r, threadName)
        }
      })
    Resource.make(IO(makeExecutor))(e => IO(e.shutdown())).map { executor =>
      val ec = ExecutionContext.fromExecutor(executor)
      implicit val contextShift = IO.contextShift(ec)
      implicit val F = ConcurrentEffect[IO]
      implicit val mockClock = MockClock.make[IO].unsafeRunSync()
      implicit val timer = IO.timer(ec)
      val contextRuntimeBuilder = new ContextRuntimeBuilder[IO] {
        override def make[T](empty: T): IO[ContextRuntime[IO, T]] = {
          Ref.of(empty).map { context =>
            new ContextRuntime[IO, T] {
              override implicit def use: UseContext[IO, T] =
                new UseContext[IO, T] {
                  override def update[V](f: T => T)(fv: IO[V]): IO[V] =
                    context.update(f) >> fv
                }

              override implicit def as: AsContext[IO, T] =
                new AsContext[IO, T] {
                  override def get: IO[T] = context.get
                }
            }
          }
        }
      }
      val bos = new ByteArrayOutputStream()
      val printStream = new PrintStream(bos)

      val level = Ref.unsafe[IO, Level](Level.Trace)
      val loggingRuntime = ConsoleFactory[IO]
        .withPrintStream(printStream)
        .make(
          Format.Plain,
          _ => level.get,
          contextRuntimeBuilder
        )
        .unsafeRunSync()

      val output = new Output[IO] {
        override def get: IO[String] =
          IO.delay(
            new String(bos.toByteArray).replaceAll("\u001B\\[[;\\d]*m", "")
          )
      }

      import loggingRuntime._
      new Fixture(loggerFactory, loggingContext, output, level)
    }
  }
}

object ConsoleLoggerTest {

  final class Fixture[F[_]](
      val loggerFactory: LoggerFactory[F],
      val loggingContext: LoggingContext[F],
      val output: Output[F],
      val level: Ref[F, Level]
  )(
      implicit val F: ConcurrentEffect[F],
      val C: MockClock[F],
      val T: Timer[F]
  ) {
    val loggerName = "test-logger"
    val logger = loggerFactory.make(loggerName)

    def validateOutput(f: String => Unit): F[Unit] = {
      output.get.flatMap(v => F.delay(f(v)))
    }
  }

  trait Output[F[_]] {
    def get: F[String]
  }

  class MockClock[F[_]](val real: Ref[F, Long], val mono: Ref[F, Long])(
      implicit F: Monad[F]
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
        mono <- Ref.of(0L)
        real <- Ref.of(0L)
      } yield new MockClock(mono, real)
    }
  }

}
