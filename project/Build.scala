import com.jsuereth.ghpages.GhPages.ghpages
import com.jsuereth.git.GitPlugin.git
import com.jsuereth.sbtsite.SitePlugin.site
import sbt._
import Keys._

object BuildLib extends Build {

  lazy val gitUser = "robinp"
  lazy val gitProject = "akkaz"

  lazy val ghPagesSettings =
    site.settings ++
    site.includeScaladoc() ++
    ghpages.settings ++
    Seq(
      git.remoteRepo := "git@github.com:" + gitUser + "/" + gitProject + ".git"
    )

  lazy val rootSettings =
    Seq(
      version := "0.0.1",
      scalaVersion := "2.9.2",
      resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases",
      libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-core" % "7.0.0-M2",
        "com.typesafe.akka" % "akka-actor" % "2.0.2",
        "org.scalaz" %% "scalaz-scalacheck-binding" % "7.0.0-M2" % "test",
        "org.specs2" %% "specs2" % "1.11" % "test"
      )
    )

  lazy val root = Project(
    id = "akkaz",
    base = file("."),
    settings = Project.defaultSettings ++ ghPagesSettings ++ rootSettings)

}