import com.typesafe.sbt.site.SitePlugin.autoImport.*
import mdoc.MdocPlugin.autoImport.*
import microsites.MicrositesPlugin.autoImport.*
import sbt.*
import sbt.Keys.*
import sbtunidoc.ScalaUnidocPlugin.autoImport.*

import scala.collection.Seq

object BuildSupport {
  lazy val commonSettings = Seq(
    crossScalaVersions := List("2.12.20", "2.13.16"),
    scalacOptions := Seq(
      "-release",
      "11",
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
    Test / scalacOptions --= Seq("-Ywarn-dead-code", "-Ywarn-value-discard"),
    versionScheme := Some("early-semver"),

    // Publishing / Sonatype
    organization := "com.avast",
    organizationName := "Avast",
    homepage := Some(url("https://avast.github.io/slog4s/")),
    licenses := Seq(
      "MIT" -> url(
        "https://raw.githubusercontent.com/avast/slog4s/master/LICENSE"
      )
    ),
    developers := List(
      Developer(
        "karry",
        "Lukas Karas",
        "lukas.karas@gendigital.com",
        url("https://www.gendigital.com")
      )
    )
  )

  lazy val micrositeSettings = Seq(
    micrositeName := "slog4s",
    micrositeDescription := "Structured and contextual logging for Scala",
    micrositeAuthor := "Jan Strnad",
    micrositeGithubOwner := "avast",
    micrositeGithubRepo := "slog4s",
    micrositeUrl := "https://avast.github.io",
    micrositeDocumentationUrl := "api/latest",
    micrositeBaseUrl := "/slog4s",
    micrositeFooterText := None,
    micrositeGitterChannel := false,
    micrositeTheme := "pattern",
    mdoc / fork := true,
    mdocIn := file("site") / "docs",
    mdocVariables := Map("VERSION" -> version.value),
    mdocAutoDependency := false,
    micrositeDataDirectory := file("site"),
    ScalaUnidoc / siteSubdirName := "api/latest",
    addMappingsToSiteDir(
      ScalaUnidoc / packageDoc / mappings,
      ScalaUnidoc / siteSubdirName
    )
  )

}
