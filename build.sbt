name := """taxoscope"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

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
  "nl.ebpi.tqa" % "tqa" % "0.5.5" excludeAll(
    ExclusionRule(organization = "org.scala-lang"),
    ExclusionRule(organization = "org.scalaz")
  ),
  "org.scalaz" %% "scalaz-core" % "7.0.6"
)

