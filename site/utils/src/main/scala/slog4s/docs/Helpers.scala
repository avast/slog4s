package slog4s.docs

import java.io.{ByteArrayOutputStream, PrintStream}

import cats.effect.IO
import slog4s.slf4j.Slf4jFactory

object Helpers {
  val stdout = new ByteArrayOutputStream()

  def init(): Unit = {
    System.setOut(new PrintStream(stdout))
  }

  def output(): String = {
    new String(stdout.toByteArray)
  }

  object instances {

    val loggerFactory = Slf4jFactory[IO].withoutContext.loggerFactory
    val logger = loggerFactory.make("test-logger")
  }
}
