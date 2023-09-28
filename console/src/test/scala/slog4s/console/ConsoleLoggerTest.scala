package slog4s.console

import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.concurrent.{Executors, ThreadFactory}
import cats.effect.{ConcurrentEffect, IO, Resource, Sync}
import cats.syntax.all._
import slog4s.Location.Code
import slog4s.console.ConsoleLoggerTest.{Fixture, Output}
import slog4s.shared.{
  AsContext,
  ContextRuntime,
  ContextRuntimeBuilder,
  UseContext
}
import slog4s._

import scala.concurrent.ExecutionContext
import cats.effect.{ Ref, Temporal }

abstract class ConsoleLoggerTest[F[_]](format: Format) extends EffectTest[F] {
  override protected def asEffect(
      fixtureParam: FixtureParam
  ): ConcurrentEffect[F] = fixtureParam.F

  override protected def asTimer(fixtureParam: FixtureParam): Temporal[F] =
    fixtureParam.T

  override type FixtureParam = ConsoleLoggerTest.Fixture[F]

  val testMessage = "test message"
  val testFile = "TestFile.scala"
  val testLine = 42
  implicit val location: Code = Location.Code(testFile, testLine)
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
      implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)
      implicit val F: Sync[IO] = ConcurrentEffect[IO]
      implicit val mockClock: MockClock[IO] = MockClock.make[IO].unsafeRunSync()
      implicit val timer: Temporal[IO] = IO.timer(ec)
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
        .makeFromBuilder(
          format,
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
  )(implicit
      val F: ConcurrentEffect[F],
      val C: MockClock[F],
      val T: Temporal[F]
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

}
