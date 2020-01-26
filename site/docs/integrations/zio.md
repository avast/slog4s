---
layout: docs
title:  "ZIO"
---

# Monix
 
Module `slog4s-zio` provides basic integration with [ZIO](https://zio.dev/) effect system. It 
leverages [`FiberRef`](https://zio.dev/docs/datatypes/datatypes_fiberref) for context propagation.

## slf4j example

This example demonstrates how to make an slf4j specific instance of `LoggingContext` and `LoggerFactory` 
backed by ZIO's `FiberRef`. 

```scala mdoc
import cats.effect.Sync
import slog4s._
import slog4s.shared._
import slog4s.slf4j._
import slog4s.zio._
import _root_.zio._

def make(implicit F: Sync[Task]): Task[LoggingRuntime[Task]] = {
  Slf4jFactory[Task].fromContextBuilder(ZIOContextRuntimeBuilder.Task)
}
```
