package slog4s.testkit

import java.io.{ByteArrayOutputStream, PrintStream}
import java.time.Instant

import cats.effect.{ConcurrentEffect, ContextShift, IO, Sync, Timer}
import org.scalatest.Outcome
import slog4s.{EffectTest, Level, Location, MockClock}

import scala.concurrent.ExecutionContext
import scala.util.control.NoStackTrace

class TestkitTest extends EffectTest[IO] {

  describe("Testkit") {
    describe("Stores log events") {
      it_("Globally") { fixture =>
        import TestData._
        import fixture._
        val logger = loggingRuntime.loggerFactory.make("logger")
        for {
          _ <- logger.info(msg)
          events <- loggingRuntime.loggerFactory.events
          _ <- validate {
            assert(events.size === 1)
            assert(events.headOption.get.message === msg)
          }
        } yield ()
      }
      it_("For individual logger") { fixture =>
        import TestData._
        import fixture._
        val logger = loggingRuntime.loggerFactory.make("logger")
        val logger2 = loggingRuntime.loggerFactory.make("logger2")
        for {
          _ <- logger.info(msg)
          _ <- logger2.info(msg)
          events <- logger.events
          _ <- validate {
            assert(events.size === 1)
            assert(events.headOption.get.message === msg)
          }
        } yield ()
      }
    }
    describe("Mocked context") {
      it_("Is seen by a logger") { fixture =>
        import TestData._
        import fixture._
        val logger = loggingRuntime.loggerFactory.make("logger")
        loggingRuntime.loggingContext.withArg(key, value).use {
          for {
            _ <- logger.info(msg)
            events <- loggingRuntime.loggerFactory.events
            _ <- validate {
              assert(events.size === 1)
              assert(
                events.headOption.get.arguments === Map(
                  key -> Argument.String(value)
                )
              )
            }
          } yield ()
        }
      }
      it_("Is seen appended by a logger") { fixture =>
        import TestData._
        import fixture._
        val logger = loggingRuntime.loggerFactory.make("logger")
        loggingRuntime.loggingContext.withArg(key, value).use {
          for {
            _ <- logger.info.withArg(key2, value2).log(msg)
            events <- loggingRuntime.loggerFactory.events
            _ <- validate {
              assert(events.size === 1)
              assert(
                events.headOption.get.arguments === Map(
                  key -> Argument.String(value),
                  key2 -> Argument.String(value2)
                )
              )
            }
          } yield ()
        }
      }
    }
    describe("Log event") {
      it_("Has all fields") { fixture =>
        import TestData._
        import fixture._
        val logger = loggingRuntime.loggerFactory.make("logger")
        implicit val location = Location.Code("file", 42)
        for {
          _ <- mockClock.real.set(timestamp)
          _ <- logger.info.withArg(key, value).log(boom, msg)
          events <- logger.events
          _ <- validate {
            val expected = LogEvent(
              msg,
              Some(boom),
              Map(key -> Argument.String(value)),
              location,
              Instant.ofEpochMilli(timestamp),
              Level.Info,
              "logger"
            )
            assert(events === List(expected))
          }
        } yield ()
      }
      it_("Prints correctly") { fixture =>
        import TestData._
        import fixture._
        val logger = loggingRuntime.loggerFactory.make("logger")
        implicit val location = Location.Code("file", 42)
        for {
          _ <- mockClock.real.set(timestamp)
          _ <- logger.info.withArg(key, value).log(boom, msg)
          outputStream <- IO(new ByteArrayOutputStream())
          printStream <- IO(new PrintStream(outputStream))
          _ <- logger.print(printStream)
          _ <- IO(printStream.close())
          bytes <- IO(outputStream.toByteArray)
          _ <- validate {
            val output = new String(bytes)
            val expected =
              """
                |2018-12-24T09:57:45.465Z INFO logger file:42 {key => value} : message
                |boom: boom
                |""".stripMargin
            assert(output.trim === expected.trim)
          }
        } yield ()
      }
    }
  }

  object TestData {
    val msg = "message"
    val key = "key"
    val key2 = "key2"
    val value = "value"
    val value2 = "value2"
    object boom extends Exception("boom") with NoStackTrace {
      override def toString = s"boom: boom"
    }
    val timestamp = 1545645465465L
  }

  private[this] implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  private[this] implicit val timer: Timer[IO] =
    IO.timer(ExecutionContext.global)

  override protected def asEffect(
      fixtureParam: FixtureParam
  ): ConcurrentEffect[IO] = IO.ioConcurrentEffect

  override protected def asTimer(
      fixtureParam: FixtureParam
  ): Timer[IO] = timer

  override protected def withFixture(test: OneArgTest): Outcome = {
    val ops = for {
      mockClock <- MockClock.make[IO]
      mockLoggingRuntime <- MockLoggingRuntime.make[IO](Sync[IO], mockClock)
    } yield {
      val fixture = new Fixture(mockClock, mockLoggingRuntime)
      test(fixture)
    }
    ops.unsafeRunSync()
  }

  override type FixtureParam = Fixture
}
