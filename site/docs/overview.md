---
layout: docs
title:  "Overview"
position: 0
---

# Modules overview

`slog4s` is split into multiple modules:
- `slog4s-api`: top level API that should be used by libraries
- `slog4s-generic`: brings support for automatic derivation for ADT
- `slog4s-monix`: provides implementation of `LoggingContext` specialised for [Monix](https://monix.io/)
- `slog4s-slf4j`: [slf4j](http://www.slf4j.org/) and [logstash-encoder](https://github.com/logstash/logstash-logback-encoder)
specific implementation of the logging API
