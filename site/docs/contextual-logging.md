---
layout: docs
title:  "Contextual logging"
position: 1
---

```scala mdoc:invisible
import slog.docs.Helpers
Helpers.init()
```

# Contextual logging

Contextual logging is provided by `LoggingContext` typeclass. 

`LoggingContext` is a typeclass, as its name suggest, provides propagation of logging context across multiple loggers and
log messages. It allows you to add additional arguments to all log messages produced inside given code block. Typical 
example is `Correlation-Id` (sometimes also known as `Trace-Id`) HTTP header. When an HTTP request comes in, we assign
unique random string to it (`Correlation-Id`). Using `LoggingContext` we can easily make sure that this string appears in
all log messages related to the HTTP request.

There are plenty use cases where you might want to include common argument in all related log messages: user id,
file name or any other entity. 

Here is a simple example:
```scala mdoc
import cats.Monad
import cats.syntax.all._
import slog._

def foo[F[_]:Monad:LoggingContext](loggerFactory: LoggerFactory[F]): F[Unit] = {
    val logger = loggerFactory.make("foo")
    LoggingContext[F].withArg("correlation_id", "generated-correlation-id")
                     .use {
        logger.info("Hello from foo!") >> bar(loggerFactory)
    }
}

def bar[F[_]:LoggingContext](loggerFactory: LoggerFactory[F]): F[Unit] = {
    val logger = loggerFactory.make("bar")
    logger.info("Hellow from bar!")
}
```

No we just need to get  `LoggingContext` and `LoggerFactory` instances. We will use slf4j module for that. We will also 
use `ReaderT[IO, Slf4jArgs, ?]` as our effect type because the implementation requires our effect implement 
`ApplicationLocal` typeclass. There are more efficient implementations for different effect types that relies on other
mechanisms (for curious ones: it `TaskLocal` for Monix and `FiberRef` for ZIO) 

```scala mdoc:silent
import cats.data._
import cats.effect._
import cats.mtl.instances.local._
import slog.slf4j._
type Result[T] = ReaderT[IO, Slf4jArgs, T]
implicit val loggingContext = Slf4jContext.make[Result]
val loggerFactory = Slf4jFactory[Result].contextAsk.make
```

No we can finally run it:

```scala mdoc:evallog:all
// we start with empty additional arguments
foo(loggerFactory).run(Slf4jArgs.empty).unsafeRunSync()
```
