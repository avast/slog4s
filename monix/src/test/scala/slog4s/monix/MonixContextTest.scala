package slog4s.monix

import monix.eval.{Task, TaskLocal}
import monix.execution.Scheduler
import org.scalatest.Outcome
import slog4s.shared.ContextTest
import slog4s.shared.ContextTest.Fixture

class MonixContextTest extends ContextTest[Task]("Monix") {
  override protected def withFixture(test: OneArgTest): Outcome = {
    implicit val F = Task.catsEffect(
      Scheduler.traced,
      Task.defaultOptions.enableLocalContextPropagation
    )
    val ops = TaskLocal(0).flatMap { taskLocal =>
      implicit val asContext = AsMonixContext.identity(taskLocal)
      implicit val useContext = UseMonixContext.identity(taskLocal)

      val fixture = new Fixture
      Task.delay(test(fixture))
    }
    F.toIO(ops).unsafeRunSync()
  }
}
