package slog4s.console.internal

import cats.data.NonEmptyList
import cats.effect.Sync
import slog4s.Level
import slog4s.console.ConsoleConfig
import slog4s.console.internal.StructuredConsoleConfig.Node

private[console] class StructuredConsoleConfig[F[_]](
    rootNode: Node
)(implicit F: Sync[F])
    extends ConsoleConfig[F] {
  override def level(name: String): F[Level] = F.delay {
    val splitName = name.split('.').toList
    go(splitName, rootNode)
  }

  private def go(name: List[String], node: Node): Level = {
    name match {
      case name :: rest =>
        node.childs.get(name).map(go(rest, _)).getOrElse(node.level)
      case Nil => node.level
    }
  }
}

private[console] object StructuredConsoleConfig {
  private final case class Node(level: Level, childs: Map[String, Node])

  def make[F[_]: Sync](
      rootLevel: Level,
      levels: Map[String, Level]
  ): StructuredConsoleConfig[F] = {
    val nonEmptyLevels = levels
      .map {
        case (name, level) =>
          NonEmptyList.fromListUnsafe(name.split('.').toList) -> level
      }

    val rootNode = Node(rootLevel, makeNode(rootLevel, nonEmptyLevels))
    new StructuredConsoleConfig[F](rootNode)
  }

  private def makeNode(
      rootLevel: Level,
      levels: Map[NonEmptyList[String], Level]
  ): Map[String, Node] = {
    levels.groupBy(_._1.head).map {
      case (name, values) =>
        val selfLevel = values.getOrElse(NonEmptyList.one(name), rootLevel)
        val childs = values.collect {
          case (NonEmptyList(_, next :: rest), level) =>
            NonEmptyList.of(next, rest: _*) -> level
        }
        name -> Node(selfLevel, makeNode(selfLevel, childs))
    }
  }
}
