import BuildSupport._
import Dependencies._

ThisBuild / scalaVersion := "2.12.10"
ThisBuild / organization := "com.avast"
ThisBuild / organizationName := "avast"

inThisBuild(
  List(
    organization := "com.avast",
    homepage := Some(url("https://github.com/avast/slog4s")),
    licenses := List(
      "MIT" -> url("https://github.com/avast/slog4s/blob/master/LICENSE")
    ),
    developers := List(
      Developer(
        "hanny24",
        "Jan Strnad",
        "strnad@avast.com",
        url("https://github.com/hanny24")
      )
    )
  )
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "slog4s",
    crossScalaVersions := Nil
  )
  .aggregate(api, core, example, generic, monix, slf4j)

lazy val api = (project in file("api"))
  .settings(
    name := "slog4s-api"
  )
  .settings(commonSettings)
  .dependsOn(core)

lazy val core = (project in file("core"))
  .settings(
    name := "slog4s-core",
    libraryDependencies += catsCore,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
  .settings(commonSettings)

lazy val example = (project in file("example"))
  .settings(
    name := "slog4s-example",
    libraryDependencies ++= Seq(catsCore, logback, logstash, monixDependency)
  )
  .settings(commonSettings)
  .settings(publish / skip := true)
  .dependsOn(api, generic, slf4j, monix)

lazy val generic = (project in file("generic"))
  .settings(
    name := "slog4s-generic",
    libraryDependencies += magnolia,
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(core)

lazy val monix = (project in file("monix"))
  .settings(
    name := "slog4s-monix",
    libraryDependencies ++= Seq(catsMtl, monixDependency)
  )
  .settings(commonSettings)

lazy val slf4j = (project in file("slf4j"))
  .settings(
    name := "slog4s-slf4j",
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

lazy val siteUtils = (project in file("site") / "utils")
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      circeParser,
      libmdoc,
      logback,
      logstash
    )
  )
  .dependsOn(slf4j)

lazy val site = (project in file("site"))
  .enablePlugins(
    MicrositesPlugin,
    MdocPlugin,
    SiteScaladocPlugin,
    ScalaUnidocPlugin
  )
  .settings(publish / skip := true)
  .settings(micrositeSettings: _*)
  .settings(
    // do not provide scaladoc for example
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(
      example
    )
  )
  .dependsOn(api, core, generic, monix, slf4j, siteUtils)

addCommandAlias("check", "; scalafmtSbtCheck; scalafmtCheckAll; site/makeMdoc")
addCommandAlias("fix", "; scalafmtSbt; scalafmtAll")
