import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  crossScalaVersions := List("2.12.10", "2.13.1"),
  scalacOptions := Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Ywarn-value-discard",
    "-Xfatal-warnings",
    "-deprecation",
    "-Xlint:-unused,_"
  ),
  scalacOptions in Test --= Seq("-Ywarn-dead-code", "-Ywarn-value-discard")
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "slog",
    crossScalaVersions := Nil
  )
  .aggregate(api, core, example, generic, monix, slf4j)

lazy val api = (project in file("api"))
  .settings(
    name := "slog-api"
  )
  .settings(commonSettings)
  .dependsOn(core)

lazy val core = (project in file("core"))
  .settings(
    name := "slog-core",
    libraryDependencies += catsCore,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
  .settings(commonSettings)

lazy val example = (project in file("example"))
  .settings(
    name := "slog-example",
    libraryDependencies ++= Seq(catsCore, logback, logstash, monixDependency)
  )
  .settings(commonSettings)
  .dependsOn(api, generic, slf4j, monix)

lazy val generic = (project in file("generic"))
  .settings(
    name := "slog-generic",
    libraryDependencies += magnolia,
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(core)

lazy val monix = (project in file("monix"))
  .settings(
    name := "slog-slf4j",
    libraryDependencies ++= Seq(catsMtl, monixDependency)
  )
  .settings(commonSettings)

lazy val slf4j = (project in file("slf4j"))
  .settings(
    name := "slog-slf4j",
    libraryDependencies ++= Seq(
      catsCore,
      catsEffect,
      catsMtl,
      logstash,
      scalaCollectionCompat,
      slf4jDepedency
    ),
    libraryDependencies ++= Seq(
      diffx % Test,
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(api, core)
