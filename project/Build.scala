import sbt._
import Keys._

object BuildLib extends Build {

  lazy val rootSettings = Seq(
    version := "0.0.1",
    scalaVersion := "2.9.2",
    resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.0.0-M1",
      "com.typesafe.akka" % "akka-actor" % "2.0.2",
      "org.scalaz" %% "scalaz-scalacheck-binding" % "7.0.0-M1" % "test",
      "org.specs2" %% "specs2" % "1.11" % "test"
    )
  )

  lazy val root = Project(
    id = "akkaz",
    base = file("."),
    settings = Project.defaultSettings ++ rootSettings)

}