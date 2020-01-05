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
libraryDependencies ++= Seq("com.avast" %% "slog4s-generic" % "@VERSION@") 
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

## Map support

There is built-in support for representing `Map`s. Generally we try to represent them as a map in the target encoding
(think JSON dictionary) whenever possible. However it might be impossible for cases where a key is not a primitive type
or a `String`. So there is a simple rule: if the key implements `cats.Show` typeclass, we represent the whole `Map`
as a real map/dictionary. Otherwise we represent it as an array of key/value pairs.

```scala mdoc:evallog
import cats.Show
import cats.instances.all._

case class MyKey(value: String)
object MyKey {
  implicit val showInstance: Show[MyKey] = _.value
}

logger.info
      .withArg("foo", Map("key" -> "value"))
      .withArg("bar", Map(42 -> "value"))
      .withArg("baz", Map(MyKey("my_key") -> "value"))
      .log("Hello world")
      .unsafeRunSync()
```

```scala mdoc:evallog
case class OtherKey(x: Int, y: Int)

logger.info
      .withArg("foo", Map(OtherKey(1,2) -> "value"))
      .log("Hello world")
      .unsafeRunSync()
``` 
