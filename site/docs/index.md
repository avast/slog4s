---
layout: home
title:  "Home"
section: "home"
---

This is a quick start with *slog4s* on the JVM with [logback](http://logback.qos.ch/) backend. It doesn't 
mean we are limited to JVM or logback! Au contraire!

# Installation

Add new library dependencies to your `build.sbt` 

```scala
libraryDependencies ++= Seq("com.avast" %% "slog4s-api" % "@VERSION@", 
                            "com.avast" %% "slog4s-slf4j" % "@VERSION@")
```

We will be using logback with [logstash encoder](https://github.com/logstash/logstash-logback-encoder). So make sure 
it's in your dependency list and it's configured properly.

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
    </appender>
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

# Usage

Obligatory imports before we start:
```scala mdoc
import cats.effect._
import cats.syntax.all._
import slog4s._
import slog4s.slf4j._
``` 

```scala mdoc:invisible
import slog4s.docs.Helpers
Helpers.init()
```


Let's start by creating a `LoggerFactory` that is backed by slf4j (and logback under the hood). With that, we
can create a named `Logger`.

```scala mdoc
val loggerFactory = Slf4jFactory[IO].noContext.make
val logger = loggerFactory.make("test-logger")
``` 

It's finally time to log something!

```scala mdoc:evallog
logger.info("Hello world!").unsafeRunSync()
```

That was pretty boring, except `x-file` and `x-line` attributes that denote location of the log message within a file. 
We can also provide an exception:

```scala mdoc:evallog
logger.error(new Exception("Boom!"), "Something went horribly wrong.").unsafeRunSync()
```

Let's make our message more content rich with additional arguments:

```scala mdoc:evallog
logger.info
      .withArg("string_value", "<VALUE>")
      .withArg("bool_value", true)
      .withArg("list_value", List(1,2,3))
      .log("Message with arguments")
      .unsafeRunSync()
``` 

Additional arguments are type safe. Following code will fail in compile time:

```scala mdoc:fail
class That(value: String)
logger.info
      .withArg("that_value", new That("value"))
      .log("Does not compile")
      .unsafeRunSync()
```

However there is built-in support for `case class`es and `sealed trait`s provided by `generic` module.
