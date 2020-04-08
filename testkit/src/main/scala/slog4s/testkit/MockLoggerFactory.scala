package slog4s.testkit

import java.io.PrintStream

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{Clock, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import slog4s.LoggerFactory
import slog4s.testkit.internal.Printer

/**
  * Mock implementation of [[LoggerFactory]]. Can be used assert that specific [[LogEvents]] occurred.
  */
class MockLoggerFactory[F[_]: Sync: Clock](
    logEvents: Ref[F, LogEvents],
    getArguments: F[Arguments]
) extends LoggerFactory[F] {
  override def make(name: String): MockLogger[F] = {
    new MockLogger[F](logEvents, name, getArguments)
  }

  /**
    * Removes all log events
    */
  def drain: F[Unit] = logEvents.set(List.empty)

  /**
    * Gets all log events
    */
  def events: F[LogEvents] = logEvents.get

  /**
    * Prints all log events to provided stream.
    */
  def print(printStream: PrintStream = System.out): F[Unit] = {
    logEvents.get.flatMap { allEvents =>
      Printer.printLogEvents(allEvents, printStream)
    }
  }
}

object MockLoggerFactory {

  /**
    * Makes a fresh instance of [[MockLoggerFactory]].
    */
  def make[F[_]: Sync: Clock]: F[MockLoggerFactory[F]] =
    withArguments(Applicative[F].pure(Map.empty))

  /**
    * Makes a fresh instance of [[MockLoggerFactory]] with provided
    * context arguments.
    */
  def withArguments[F[_]: Sync: Clock](
      arguments: F[Arguments]
  ): F[MockLoggerFactory[F]] = {
    for {
      events <- Ref.of(List.empty[LogEvent])
    } yield new MockLoggerFactory(events, arguments)
  }
}
