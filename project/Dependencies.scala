import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  lazy val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
  lazy val mysql = "mysql" % "mysql-connector-java" % "8.0.17"
}
