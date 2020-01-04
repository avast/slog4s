---
layout: docs
title:  "ADT support"
position: 2
---

# ADT support

`slog4s` provides built-in support for automatic derivation of `LogEncoder` typeclass, which allows one to use
`case class` or `sealed trait` as additional arguments for logging. It supports both fully automatic derivation, and semi
automatic derivation.

## Installation

```scala
libraryDependencies ++= Seq("com.avast" %% "slog-generic" % "@VERSION@") 
```

```scala mdoc:invisible
import slog4s.docs.Helpers
Helpers.init()
```

## Example

Suppose we have following case class:

```scala mdoc:silent
case class Foo(fooValue: String)
case class Bar(barValue: Int, foo: Foo)

val bar = Bar(42, Foo("Hello!"))
```

```scala mdoc:invisible
import slog4s._
import Helpers.instances._
```

## Automatic derivation

With automatic derivation you just need to include proper import.

```scala mdoc:evallog
import slog4s.generic.auto._
logger.info
      .withArg("bar", bar)
      .log("Logging bar instance")
      .unsafeRunSync()
``` 

## Semi automatic derivation

Sometimes it might be more convenient to have `LogEncoder` instance have defined directly in a code. You can use
semi automatic derivation for that:

```scala mdoc:evallog
import slog4s.generic.semi._

object Foo {
  implicit val fooEncoder: LogEncoder[Foo] = logEncoder[Foo]
}

object Bar {
  implicit val barEncoder: LogEncoder[Bar] = logEncoder[Bar]
}

logger.info
      .withArg("bar", bar)
      .log("Logging bar instance")
      .unsafeRunSync()
```
