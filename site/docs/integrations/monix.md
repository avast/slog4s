---
layout: docs
title:  "Monix"
---

# Monix
 
Module `slog4s-monix` provides basic integration with [monix](https://monix.io/) effect system. It 
leverages [`TaskLocal`](https://monix.io/api/3.0/monix/eval/TaskLocal.html) for context propagation.

## slf4j example

This example demonstrates how to make an slf4j specific instance of `LoggingContext` and `LoggerFactory` 
backed by monix's `TaskLocal`. 

```scala mdoc
import monix.eval._
import slog4s._
import slog4s.shared._
import slog4s.monix._
import slog4s.slf4j._

Slf4jFactory[Task].makeFromBuilder(MonixContextRuntimeBuilder)
```
