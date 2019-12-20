package example

import cats.Monad
import cats.mtl.ApplicativeLocal
import cats.syntax.flatMap._
import cats.syntax.functor._
import monix.eval.{Task, TaskLocal}
import monix.execution.schedulers.CanBlock
import slog.monix.MonixContext
import slog.slf4j.{Slf4jArgs, Slf4jContext, Slf4jFactory}
import slog.{LoggerFactory, LoggingContext}

class Example[F[_]](
    loggerFactory: LoggerFactory[F],
    loggingContext: LoggingContext[F]
)(implicit F: Monad[F]) {
  private[this] val logger = loggerFactory.make[Example[F]]
  // or
  // private [this] val logger = loggerFactory.make("my-logger")

  // example output json does not include timestamps, log levels, thread name,
  // logger names etc to keep things simple. They are of course included in the real
  // output.
  def foo: F[Unit] = {
    // simple log message. Might take from scope.
    // {"message": "Hello world!!", "file": "Example.scala", "line": 19}
    logger.info("Hello world!!") >>
      // Provide extract context to the log message
      // {"message": "Hello", "file": "Example.scala", "line": 22, "correlation_id": "<VALUE>"}
      logger.info.withArg("correlation_id", "<VALUE>").log("Hello") >>
      // Provide extra context and an exception
      // {"message": "Hello", "file": "Example.scala", "line": 25, "correlation_id": "<VALUE>", "stack_trace": "Exception ..."}
      logger.info
        .withArg("correlation_id", "<VALUE>")
        .log(new Exception, "Hello") >>
      loggingContext
        .withArg("correlation_id", "<VALUE>")
        .withArg("request_id", "<VALUE>")
        .use {
          // {"message": "test", "file": "Example.scala", "line": 33, "correlation_id": "<VALUE>", "request_id": "<VALUE>", "stack_trace": "Exception ..."}
          logger.debug(new Exception("error"), "test") >>
            // {"message": "test2", "file": "Example.scala", "line": 35, "correlation_id": "<VALUE>", "request_id": "<VALUE>"}
            logger.info("test2")
        } >>
      logger.trace.whenEnabled { logBuilder =>
        // low level method how to do things only when certain log level is enabled
        for {
          value <- superExpensiveToCompute
          _ <- logBuilder
            .withArg("expensive", value)
            .log("This message was very expensive to compute!")
        } yield ()
      } >>
      logger.trace.whenEnabled // higher level API
        .computeArg("expensive")(superExpensiveToCompute)
        .withArg("cheap", "not expensive but to be included as well")
        .log("This message was very expensive to compute!")
  }

  val superExpensiveToCompute: F[String] = F.pure("$$$")

  def bar: F[Unit] = {
    // We can use case classes/sealed traits as context as well!
    // We use automatic typeclass deriviation here (hence the import), but
    // one can use semi automatic one as well (similar to circe)
    final case class Bar(values: List[Double])
    final case class Foo(value: Int, that: String, bar: Bar)
    import slog.generic.auto._
    // {"message": "test", "file": "Example.scala", "line": 47, "foo_bar": {"value": 10, "that": "foobar", "bar": {"values": [42.0]}}}
    logger.trace
      .withArg("foo_bar", Foo(10, "foobar", Bar(List(42.0))))
      .log("test")

    // it's also easily possible to debug failed typeclass deriviation.
    // consider following example:
    class Inner
    final case class Outer(inner: Inner)

    // StructureEncoder[Outer]
    // fails with: could not find implicit value for parameter ev: slog.StructureEncoder[Outer]

    // You just need to call derivation method directly and it should help you debug it:
    // genStructureEncoder[Outer]
    // fails with: magnolia: could not find StructureEncoder.Typeclass for type Inner
    //    in parameter 'inner' of product type Outer
    //    genStructureEncoder[Outer]
    F.unit
  }
}

object Example extends App {
  import monix.execution.Scheduler.Implicits.traced
  implicit val canBlock = CanBlock.permit

  val taskLocal: TaskLocal[Slf4jArgs] =
    TaskLocal(Slf4jArgs.empty).runSyncUnsafe()

  implicit val applicativeAsk: ApplicativeLocal[Task, Slf4jArgs] =
    MonixContext.identity(taskLocal)

  //implicit val asMarker: AsMarker[Slf4jArgs] = new AsMarker[Slf4jArgs] {
  //  override def extract(v: Slf4jArgs): Option[Marker] =
  //    Some((new BasicMarkerFactory).getMarker("INCREASE"))
  //}

  val loggerFactory =
    Slf4jFactory[Task]
      .contextAsk[Slf4jArgs]
      .withArg("app_version", "0.1.0")
      .make
  //val loggerFactory = Slf4jFactory[Task].noContext.make

  val loggingContext = Slf4jContext.make

  val example = new Example[Task](loggerFactory, loggingContext)

  val ops = example.foo >> example.bar

  ops.runSyncUnsafe()

}
