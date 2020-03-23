import sbt._

object Dependencies {
  lazy val catsCore = "org.typelevel" %% "cats-core" % "2.1.1"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.1.2"
  lazy val catsMtl = "org.typelevel" %% "cats-mtl-core" % "0.7.1"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.13.0"
  lazy val circeLiteral =
    "io.circe" %% "circe-literal" % "0.13.0" // used for test only
  lazy val circeParser =
    "io.circe" %% "circe-parser" % "0.13.0" // used for site only
  lazy val diffx = "com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.18"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val logstash =
    "net.logstash.logback" % "logstash-logback-encoder" % "6.3"
  lazy val kindProjector =
    "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
  lazy val magnolia = "com.propensive" %% "magnolia" % "0.12.8"
  lazy val libmdoc =
    "org.scalameta" %% "mdoc" % "2.0.3" excludeAll (ExclusionRule(
      organization = "org.slf4j"
    ))
  lazy val monixDependency = "io.monix" %% "monix" % "3.1.0"
  lazy val scalaCollectionCompat =
    "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.4"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1"
  lazy val slf4jDepedency = "org.slf4j" % "slf4j-api" % "1.7.30"
  lazy val sourcecode = "com.lihaoyi" %% "sourcecode" % "0.2.1"
  lazy val zioDependency = "dev.zio" %% "zio" % "1.0.0-RC17"
  lazy val zioInterop = "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC12"

}
