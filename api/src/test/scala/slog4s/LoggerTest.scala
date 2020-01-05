package slog4s

import cats.Id
import org.scalatest.Outcome
import org.scalatest.funspec.FixtureAnyFunSpec
import slog4s.LoggerTest.{Fixture, Location}

class LoggerTest extends FixtureAnyFunSpec {
  override protected def withFixture(test: OneArgTest): Outcome =
    test(new Fixture)

  override type FixtureParam = Fixture

  describe("Logger") {
    describe("file name and line number are filled for") {
      it("debug") { fixture =>
        import fixture._
        val _ = logger.debug
        assert(
          locationAwareLogger.debugLocations.contains(
            Location("LoggerTest.scala", 18)
          )
        )
      }
      it("error") { fixture =>
        import fixture._
        val _ = logger.error
        assert(
          locationAwareLogger.errorLocations.contains(
            Location("LoggerTest.scala", 27)
          )
        )
      }
      it("info") { fixture =>
        import fixture._
        val _ = logger.info
        assert(
          locationAwareLogger.infoLocations.contains(
            Location("LoggerTest.scala", 36)
          )
        )
      }
      it("trace") { fixture =>
        import fixture._
        val _ = logger.trace
        assert(
          locationAwareLogger.traceLocations.contains(
            Location("LoggerTest.scala", 45)
          )
        )
      }
      it("warn") { fixture =>
        import fixture._
        val _ = logger.warn
        assert(
          locationAwareLogger.warnLocations.contains(
            Location("LoggerTest.scala", 54)
          )
        )
      }
    }
  }
}

object LoggerTest {

  class Fixture {
    val locationAwareLogger = new MockLocationAwareLogger
    val logger: Logger[Id] = new Logger[Id](locationAwareLogger)
  }

  final case class Location(file: String, line: Int)

  /**
    * We don't really care about [[LevelLogBuilder]] here, we just need to
    * check that macros were expanded correctly.
    */
  class MockLocationAwareLogger extends LocationAwareLogger[Id] {

    private[this] val _debugLocations = collection.mutable.Set[Location]()
    def debugLocations: Set[Location] = _debugLocations.toSet
    override def debug(filename: String, line: Int): LevelLogBuilder[Id] = {
      _debugLocations += Location(filename, line)
      null
    }

    private[this] val _errorLocations = collection.mutable.Set[Location]()
    def errorLocations: Set[Location] = _errorLocations.toSet
    override def error(filename: String, line: Int): LevelLogBuilder[Id] = {
      _errorLocations += Location(filename, line)
      null
    }

    private[this] val _infoLocations = collection.mutable.Set[Location]()
    def infoLocations: Set[Location] = _infoLocations.toSet
    override def info(filename: String, line: Int): LevelLogBuilder[Id] = {
      _infoLocations += Location(filename, line)
      null
    }

    private[this] val _traceLocations = collection.mutable.Set[Location]()
    def traceLocations: Set[Location] = _traceLocations.toSet
    override def trace(filename: String, line: Int): LevelLogBuilder[Id] = {
      _traceLocations += Location(filename, line)
      null
    }

    private[this] val _warnLocations = collection.mutable.Set[Location]()
    def warnLocations: Set[Location] = _warnLocations.toSet
    override def warn(filename: String, line: Int): LevelLogBuilder[Id] = {
      _warnLocations += Location(filename, line)
      null
    }
  }

}
