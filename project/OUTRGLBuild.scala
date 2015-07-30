import sbt._
import Keys._

object OUTRGLBuild extends Build {
  import Dependencies._

  lazy val root = Project(id = "root", base = file(".")) aggregate(core, lwjgl, jglfw, android, ios, tools)
  lazy val core = project("core").withDependencies(gdx.core, powerscala.property)
  lazy val lwjgl = project("lwjgl").dependsOn(core).withDependencies(gdx.lwjgl)
  lazy val jglfw = project("jglfw").dependsOn(core).withDependencies(gdx.jglfw)
  lazy val android = project("android").dependsOn(core).withDependencies(google.android, gdx.android)
  lazy val ios = project("ios").dependsOn(core).withDependencies(gdx.ios)
  lazy val tools = project("tools").dependsOn(lwjgl).withDependencies(gdx.tools)

  private def project(projectName: String) = Project(id = projectName, base = file(projectName)).settings(
    name := s"${Details.name}-$projectName",
    version := Details.version,
    organization := Details.organization,
    scalaVersion := Details.scalaVersion,
    sbtVersion := Details.sbtVersion,
    fork := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= Seq(
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
    ),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomExtra := <url>${Details.url}</url>
      <licenses>
        <license>
          <name>${Details.licenseType}</name>
          <url>${Details.licenseURL}</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <developerConnection>scm:${Details.repoURL}</developerConnection>
        <connection>scm:${Details.repoURL}</connection>
        <url>${Details.projectURL}</url>
      </scm>
      <developers>
        <developer>
          <id>${Details.developerId}</id>
          <name>${Details.developerName}</name>
          <url>${Details.developerURL}</url>
        </developer>
      </developers>
  )

  implicit class EnhancedProject(project: Project) {
    def withDependencies(modules: ModuleID*) = project.settings(libraryDependencies ++= modules)
  }
}

object Details {
  val organization = "com.outr"
  val name = "outrgl"
  val version = "1.0.0-SNAPSHOT"
  val url = "http://outr.com"
  val licenseType = "Proprietary"
  val licenseURL = "http://outr.com"
  val projectURL = "https://gitlab.com/outr/outrgl"
  val repoURL = "https://gitlab.com/outr/outrgl.git"
  val developerId = "darkfrog"
  val developerName = "Matt Hicks"
  val developerURL = "http://matthicks.com"

  val sbtVersion = "0.13.8"
  val scalaVersion = "2.11.7"
}

object Dependencies {
  val gdxVersion = "1.6.4"
  val powerscalaVersion = "1.6.10"

  object google {
    val android = "com.google.android" % "android" % "4.1.1.4" % "provided"
  }

  object gdx {
    val core = "com.badlogicgames.gdx" % "gdx" % gdxVersion
    val lwjgl = "com.badlogicgames.gdx" % "gdx-backend-lwjgl" % gdxVersion
    val jglfw = "com.badlogicgames.gdx" % "gdx-backend-jglfw" % gdxVersion
    val android = "com.badlogicgames.gdx" % "gdx-backend-android" % gdxVersion
    val ios = "com.badlogicgames.gdx" % "gdx-backend-robovm" % gdxVersion
    val tools = "com.badlogicgames.gdx" % "gdx-tools" % gdxVersion
  }
  object powerscala {
    val property = "org.powerscala" %% "powerscala-property" % powerscalaVersion
  }
}