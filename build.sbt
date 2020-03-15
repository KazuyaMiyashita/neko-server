import Dependencies._

ThisBuild / scalaVersion     := "0.22.0-RC1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  // scalacOptions ++= "-deprecation" :: "-feature" :: "-Xlint" :: Nil,
  scalacOptions in (Compile, console) ~= {_.filterNot(_ == "-Xlint")},
  // scalafmtOnCompile := true,
  scalacOptions ++= { if (isDotty.value) Seq("-language:Scala2Compat") else Nil }
)

lazy val root = (project in file("."))
  .settings(
    name := "neko-server",
    commonSettings,
    libraryDependencies ++= Seq(
      scalaTest % Test,
      scalaParser
    ).map(_.withDottyCompat(scalaVersion.value))
  )

lazy val chat = (project in file("chat"))
  .settings(
    name := "neko-server-chat",
    commonSettings,
    libraryDependencies ++= Seq(
      scalaTest % Test,
      mysql
    ).map(_.withDottyCompat(scalaVersion.value))
  )
  .dependsOn(root)

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
