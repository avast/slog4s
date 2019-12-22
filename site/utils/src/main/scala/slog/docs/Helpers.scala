package slog.docs

import java.io.{ByteArrayOutputStream, PrintStream}

object Helpers {
  val stdout = new ByteArrayOutputStream()

  def init(): Unit = {
    System.setOut(new PrintStream(stdout))
  }

  def output(): String = {
    new String(stdout.toByteArray)
  }
}
