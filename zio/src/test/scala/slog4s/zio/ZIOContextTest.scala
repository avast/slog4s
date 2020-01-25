package slog4s.zio

import org.scalatest.Outcome
import slog4s.shared.ContextTest.Fixture
import slog4s.shared.{AsContext, ContextTest, UseContext}
import zio.{Task, _}
import zio.interop.catz._
import zio.interop.catz.implicits._

class ZIOContextTest extends ContextTest[Task]("ZIO") {
  override protected def withFixture(test: OneArgTest): Outcome = {
    val ops =
      FiberRef.make(0, (first: Int, _: Int) => first).flatMap { fiberRef =>
        Task.concurrentEffectWith { implicit F =>
          implicit val asContext: AsContext[Task, Int] =
            AsZIOContext.make(fiberRef)
          implicit val useContext: UseContext[Task, Int] =
            UseZIOContext.make(fiberRef)
          val fixture = new Fixture[Task]
          Task.effect(test(fixture))
        }
      }
    val runtime = new DefaultRuntime {}
    runtime.unsafeRun(ops)
  }
}
