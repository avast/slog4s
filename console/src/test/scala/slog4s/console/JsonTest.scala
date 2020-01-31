package slog4s.console

import cats.effect.IO
import io.circe.Json
import io.circe.literal._
import org.scalatest.{Inside, Outcome}

class JsonTest extends ConsoleLoggerTest[IO](Format.Json) with Inside {
  override protected def withFixture(test: OneArgTest): Outcome = {
    makeIOFixture.use(fixture => IO(test(fixture))).unsafeRunSync()
  }

  def validateJson(
      f: Json => Unit
  )(implicit fixture: FixtureParam): IO[Unit] = {
    fixture.output.get.flatMap { out =>
      IO.fromEither(io.circe.parser.parse(out))
        .flatMap(json => IO.delay(f(json)))
    }
  }

  describe("JSON console logger") {
    it_("shows message") { implicit fixture =>
      import fixture._
      for {
        _ <- logger.info(testMessage)
        _ <- validateJson { result =>
          assert(
            result === json""" {
                  "file" : "TestFile.scala",
                  "level" : "INFO",
                  "line" : 42,
                  "logger" : "test-logger",
                  "message" : "test message",
                  "thread" : "test-thread",
                  "timestamp" : "1970-01-01T00:00:00Z"
                }"""
          )
        }
      } yield ()
    }
    it_("shows an exception") { implicit fixture =>
      import fixture._
      for {
        _ <- logger.info(boom, testMessage)
        _ <- validateJson { result =>
          inside(result.hcursor.downField("exception").as[Json]) {
            case Right(exception) =>
              assert(
                exception.hcursor.downField("message").as[String] === Right(
                  "boom!"
                )
              )
              assert(
                exception.hcursor
                  .downField("stack_trace")
                  .as[List[Json]]
                  .isRight
              )
          }
        }
      } yield ()
    }
    it_("shows string argument") { implicit fixture =>
      import fixture._
      for {
        _ <- logger.info.withArg("foo", "bar").log(testMessage)
        _ <- validateJson { result =>
          assert(
            result ===
              json""" {
                  "file" : "TestFile.scala",
                  "foo": "bar",
                  "level" : "INFO",
                  "line" : 42,
                  "logger" : "test-logger",
                  "message" : "test message",
                  "thread" : "test-thread",
                  "timestamp" : "1970-01-01T00:00:00Z"
                }"""
          )
        }
      } yield ()
    }
    it_("respects log level") { implicit fixture =>
      import fixture._
      for {
        _ <- level.set(Level.Info)
        _ <- logger.debug(testMessage)
        _ <- logger.info(testMessage)
        _ <- validateJson { result =>
          assert(
            result === json""" {
                  "file" : "TestFile.scala",
                  "level" : "INFO",
                  "line" : 42,
                  "logger" : "test-logger",
                  "message" : "test message",
                  "thread" : "test-thread",
                  "timestamp" : "1970-01-01T00:00:00Z"
                }"""
          )
        }
      } yield ()
    }
  }
}
