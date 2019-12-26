package slog.docs

import java.util.concurrent.ConcurrentHashMap

import mdoc.{PostModifier, PostModifierContext}

class EvalLog extends PostModifier {
  override val name: String = "evallog"

  override def process(ctx: PostModifierContext): String = {
    val all = ctx.variables.exists(_.name == "all")
    val out = Helpers.output()
    val rawMessages = out
      .split('\n')
      .map { line =>
        val json = io.circe.parser.parse(line).right.get
        val file = json.hcursor.downField("x-file").as[String].right.get
        val lineNumber = json.hcursor.downField("x-line").as[Int].right.get
        ((file, lineNumber), json.spaces2SortKeys)
      }
    val logMessages =
      if (all) {
        rawMessages
      } else {
        rawMessages
          .filter {
            case (location, _) =>
              !EvalLog.seen.containsKey(location)
          }
          .take(1)
      }
    logMessages.foreach {
      case (location, _) => EvalLog.seen.putIfAbsent(location, ())
    }
    val prettyOutput = logMessages.map(_._2).mkString("\n")
    s"""
      |```scala
      |${ctx.originalCode.text}
      |```
      |
      |Output:
      |```json
      |$prettyOutput
      |```
      |""".stripMargin
  }
}

private object EvalLog {
  val seen = new ConcurrentHashMap[(String, Int), Unit]()
}
