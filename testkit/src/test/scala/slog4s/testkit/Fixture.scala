package slog4s.testkit

import cats.effect.IO
import slog4s.MockClock

class Fixture(
    val mockClock: MockClock[IO],
    val loggingRuntime: MockLoggingRuntime[IO]
)
