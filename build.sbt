import BuildSupport._
import Dependencies._

ThisBuild / scalaVersion := "2.12.18"
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
  .aggregate(api, console, example, generic, monix, slf4j, shared, zio)

lazy val api = (project in file("api"))
  .settings(
    name := "slog4s-api"
  )
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(catsCore, sourcecode),
    libraryDependencies ++= Seq(
      catsEffect % Test,
      scalaTest % Test
    )
  )

lazy val console = (project in file("console"))
  .settings(
    name := "slog4s-console"
  )
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(catsCore, catsEffect, circeCore),
    libraryDependencies ++= Seq(
      scalaTest % Test,
      circeLiteral % Test,
      circeParser % Test
    )
  )
  .dependsOn(
    api % "compile->compile;test->test",
    shared % "compile->compile;test->test"
  )

lazy val example = (project in file("example"))
  .settings(
    name := "slog4s-example",
    libraryDependencies ++= Seq(catsCore, logback, logstash, monixDependency)
  )
  .settings(commonSettings)
  .settings(publish / skip := true)
  .dependsOn(api, console, generic, slf4j, monix)

lazy val generic = (project in file("generic"))
  .settings(
    name := "slog4s-generic",
    libraryDependencies ++= Seq(
      magnolia,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided
    ),
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(api)

lazy val monix = (project in file("monix"))
  .settings(
    name := "slog4s-monix",
    libraryDependencies ++= Seq(catsMtl, monixDependency),
    libraryDependencies ++= Seq(
      diffx % Test,
      diffxShould % Test,
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(shared % "compile->compile;test->test")

lazy val slf4j = (project in file("slf4j"))
  .settings(
    name := "slog4s-slf4j",
    libraryDependencies ++= Seq(
      catsCore,
      catsEffect,
      logstash,
      scalaCollectionCompat,
      slf4jDepedency
    ),
    libraryDependencies ++= Seq(
      diffx % Test,
      diffxShould % Test,
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(api, shared)

lazy val shared = (project in file("shared"))
  .settings(
    name := "slog4s-shared",
    addCompilerPlugin(kindProjector),
    libraryDependencies ++= Seq(
      catsCore,
      catsEffect,
      catsMtl
    ),
    libraryDependencies ++= Seq(
      diffx % Test,
      diffxShould % Test,
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(api % "compile->compile;test->test")

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
    addCompilerPlugin(kindProjector),
    // do not provide scaladoc for example
    ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(
      example
    )
  )
  .dependsOn(api, generic, monix, slf4j, siteUtils, zio)

lazy val testkit = (project in file("testkit"))
  .settings(
    name := "slog4s-testkit",
    libraryDependencies ++= Seq(
      Dependencies.catsEffect
    )
  )
  .settings(commonSettings)
  .dependsOn(api % "compile->compile;test->test", shared)

lazy val zio = (project in file("zio"))
  .settings(
    addCompilerPlugin(kindProjector),
    libraryDependencies ++= Seq(
      zioDependency,
      zioInterop
    ),
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )
  .settings(commonSettings)
  .dependsOn(api, shared % "compile->compile;test->test")

addCommandAlias(
  "check",
  "; scalafmtSbtCheck; scalafmtCheckAll; doc; site/unidoc; site/makeMdoc"
)
addCommandAlias("fix", "; scalafmtSbt; scalafmtAll")
