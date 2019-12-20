# Structured logging
A library that provides structured and contextual logging for scala.

## Goals
 * log commands are side-effecting programs, based on `cats-effect`
 * objects provided to log commands should appear naturally based on target
   * JSON for Kibana, Loggly and similar solution
   * human readable string for console/text file
 * logs will contain appropriate context
   * the context can be programmatically augmented
   * works in a stack-like manner, including shadowing
   * works well with `cats-effect` and related libraries (is not bound to `ThreadLocal`/fat JVM thread like slf4j's MDC)
 * contains other useful metadata in logs out of the box
   * timestamp
   * file name
   * line number
   * loglevel as both string and number
 * is extensible: very little is assumed
 * uses minimal dependencies
 
 ## Example
 
 See an example [here](example).