package slog4s.console

import cats.effect.IO
import org.scalatest.Outcome

class PlainTest extends ConsoleLoggerTest[IO](Format.Plain) {
  override protected def withFixture(test: OneArgTest): Outcome = {
    makeIOFixture.use(fixture => IO(test(fixture))).unsafeRunSync()
  }

  describe("Plaint console logger") {
    it_("shows message") { fixture =>
      import fixture._
      for {
        _ <- logger.info(testMessage)
        _ <- validateOutput { out =>
          assert(
            out.trim === "1970-01-01T00:00:00Z [test-thread] INFO test-logger (TestFile.scala:42) : test message"
          )
        }
      } yield ()
    }
    it_("shows an exception") { fixture =>
      import fixture._
      val expectedException: String =
        """
        |1970-01-01T00:00:00Z [test-thread] INFO test-logger (TestFile.scala:42) : test message
        |java.lang.Exception: boom!
        |""".stripMargin
      for {
        _ <- logger.info(boom, testMessage)
        _ <- validateOutput { out =>
          assert(out.trim.startsWith(expectedException.trim))
        }
      } yield ()
    }
    it_("shows string argument") { fixture =>
      import fixture._
      for {
        _ <- logger.info.withArg("foo", "bar").log(testMessage)
        _ <- validateOutput { out =>
          assert(
            out.trim === "1970-01-01T00:00:00Z [test-thread] INFO test-logger (TestFile.scala:42) foo=bar : test message"
          )
        }
      } yield ()
    }
    it_("respects log level") { fixture =>
      import fixture._
      for {
        _ <- level.set(Level.Info)
        _ <- logger.debug(testMessage)
        _ <- logger.info(testMessage)
        _ <- validateOutput { out =>
          assert(out.trim === "1970-01-01T00:00:00Z [test-thread] INFO test-logger (TestFile.scala:42) : test message")
        }
      } yield ()
    }
  }

}
