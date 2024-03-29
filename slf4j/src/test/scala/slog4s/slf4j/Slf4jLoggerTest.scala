package slog4s.slf4j

import cats.effect.IO
import com.softwaremill.diffx.scalatest.DiffShouldMatcher._
import com.softwaremill.diffx.generic.auto._
import net.logstash.logback.marker.MapEntriesAppendingMarker
import org.scalatest.funspec.FixtureAnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Inside, Outcome}
import org.slf4j.Marker
import org.slf4j.helpers.BasicMarkerFactory
import slog4s.slf4j.MockLogger.Message
import slog4s.{LevelLogBuilder, Location, Logger}

import scala.jdk.CollectionConverters._

class Slf4jLoggerTest extends FixtureAnyFunSpec with Matchers with Inside {

  describe("slf4j logger") {
    describe("for trace it") {
      levelTest(_.trace, _.traceMessages)
    }
    describe("for debug it") {
      levelTest(_.debug, _.debugMessages)
    }
    describe("for info it") {
      levelTest(_.info, _.infoMessages)
    }
    describe("for warn it") {
      levelTest(_.warn, _.warnMessages)
    }
    describe("for error it") {
      levelTest(_.error, _.errorMessages)
    }
  }

  override protected def withFixture(test: OneArgTest): Outcome = {
    implicit val location: Location = Location.Code(sourceFile, sourceLine)
    test(new Fixture)
  }

  override type FixtureParam = Fixture

  private val sourceFile = "testfile.scala"
  private val sourceLine = 1

  private def locationMarker(extraArgs: Slf4jArgs = Slf4jArgs.empty) =
    new MapEntriesAppendingMarker(
      (Map[String, Any](
        Slf4jLogger.FileKey -> sourceFile,
        Slf4jLogger.LineKey -> sourceLine
      ) ++ extraArgs).asJava
    )

  private def levelTest(
      selectLevel: Logger[IO] => LevelLogBuilder[IO],
      selectRaw: MockLogger => List[Message]
  ): Unit = {

    it("logs simple message") { fixture =>
      import fixture._
      selectLevel(makeLogger())("test").unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (List(Message(locationMarker(), "test")))
    }

    it("logs an exception") { fixture =>
      import fixture._
      val exc = new Exception
      selectLevel(makeLogger())(exc, "test").unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (List(Message(locationMarker(), "test", exc)))
    }

    it("logs with one arg") { fixture =>
      import fixture._
      selectLevel(makeLogger())
        .withArg("key", "value")
        .log("test")
        .unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (
        List(Message(locationMarker(Map("key" -> "value")), "test"))
      )
    }

    it("logs with one arg and an exception") { fixture =>
      import fixture._
      val exc = new Exception
      selectLevel(makeLogger())
        .withArg("key", "value")
        .log(exc, "test")
        .unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (
        List(Message(locationMarker(Map("key" -> "value")), "test", exc))
      )
    }

    it("logs with many args") { fixture =>
      import fixture._
      selectLevel(makeLogger())
        .withArg("key", "value")
        .withArg("key2", "value2")
        .log("test")
        .unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (
        List(
          Message(
            locationMarker(Map("key" -> "value", "key2" -> "value2")),
            "test"
          )
        )
      )
    }

    it("logs with many args and an exception") { fixture =>
      import fixture._
      val exc = new Exception
      selectLevel(makeLogger())
        .withArg("key", "value")
        .withArg("key2", "value2")
        .log(exc, "test")
        .unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (
        List(
          Message(
            locationMarker(Map("key" -> "value", "key2" -> "value2")),
            "test",
            exc
          )
        )
      )
    }

    it("implements whenEnabled correctly when level if off") { fixture =>
      import fixture._
      rawLogger.level = MockLogger.Level.Off
      selectLevel(makeLogger())
        .whenEnabled { _ =>
          IO.delay {
            fail("When enabled callback not expected to be called")
          }
        }
        .unsafeRunSync()
    }

    it("implements whenEnabled correctly when level if on") { fixture =>
      import fixture._
      selectLevel(makeLogger())
        .whenEnabled { builder => builder.log("test") }
        .unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (List(Message(locationMarker(), "test")))
    }

    it("includes arguments from context") { fixture =>
      import fixture._
      val logger = makeLogger(args = Map("key" -> "value"))
      selectLevel(logger)("test").unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (
        List(
          Message(
            locationMarker(Map("key" -> "value")),
            "test"
          )
        )
      )
    }

    it("includes marker from context") { fixture =>
      import fixture._
      val userMarker = (new BasicMarkerFactory).getMarker("test-marker")
      val expectedMarker = locationMarker()
      expectedMarker.add(userMarker)
      val logger = makeLogger(marker = Some(userMarker))
      selectLevel(logger)("test").unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (List(Message(locationMarker(), "test")))
      inside(result) { case List(Message(Some(marker), "test", None, Nil)) =>
        assert(marker.contains(userMarker))
      }
    }

    it("uses marker to check active log level") { fixture =>
      import fixture._
      val markerFactory = new BasicMarkerFactory
      val allowedMarker = markerFactory.getMarker("allowed-marker")
      val disabledMarker = markerFactory.getMarker("disabled-marker")
      val logger = makeLogger(marker = Some(allowedMarker))
      rawLogger.expectedMarker = Some(allowedMarker)
      selectLevel(logger)("test").unsafeRunSync()
      selectRaw(rawLogger).size shouldMatchTo (1)

      rawLogger.reset()

      rawLogger.expectedMarker = Some(disabledMarker)
      selectLevel(logger)("test").unsafeRunSync()
      selectRaw(rawLogger).size shouldMatchTo (0)
    }

    it("doesn't log if level is disabled") { fixture =>
      import fixture._
      rawLogger.level = MockLogger.Level.Off
      selectLevel(makeLogger())("test").unsafeRunSync()
      val result = selectRaw(rawLogger)
      result shouldMatchTo (List.empty[Message])
    }
  }
}

class Fixture(implicit val location: Location) {
  val rawLogger = new MockLogger
  def makeLogger(
      args: Slf4jArgs = Slf4jArgs.empty,
      marker: Option[Marker] = None
  ): Logger[IO] = {
    new Slf4jLogger[IO](
      rawLogger,
      IO.pure(args),
      IO.pure(marker)
    )
  }
}
