import sbt._
import sbt.Keys._

object ApplicationBuild extends Build {
  val appName         = "play2-multimessages"

  val appVersion      = "2.4.3" //appVersion must always be in sync with play version
  val appScalaVersion = "2.10.5"
  val appScalaBinaryVersion = "2.10"
  val appScalaCrossVersions = Seq("2.10.5")

  lazy val baseSettings = Seq(
    scalaVersion := appScalaVersion,
    scalaBinaryVersion := appScalaBinaryVersion,
    crossScalaVersions := appScalaCrossVersions,
    parallelExecution in Test := false
  )

  lazy val root = Project("root", base = file("."))
    .settings(baseSettings: _*)
    .settings(
      publishLocal := {},
      publish := {}
    ).aggregate(module)

  lazy val module = Project(appName, base = file("./module"))
    .settings(baseSettings: _*)
    .settings(
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
      libraryDependencies += "com.typesafe.play" %% "play" % appVersion % "provided",
      organization := "gov.dwp",
      version := appVersion
    )
}
