name := """taxoscope"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

resolvers ++= Seq(
  Resolver.mavenLocal, 
  Resolver.sonatypeRepo("public"))

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws 
)

libraryDependencies ++= Seq(
  "nl.ebpi.tqa" % "tqa" % "0.6.0-M4" excludeAll(
    ExclusionRule(organization = "org.scala-lang"),
    ExclusionRule(organization = "org.scalaz")
  ),
  "org.scalaz" %% "scalaz-core" % "7.0.6")

