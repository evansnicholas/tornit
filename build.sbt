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
  "nl.ebpi.tqa" % "tqa" % "0.6.0-SNAPSHOT" excludeAll(
    ExclusionRule(organization = "org.scala-lang"),
    ExclusionRule(organization = "org.scalaz")
  ),
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "underscorejs" % "1.7.0",
  "org.webjars" % "requirejs" % "2.1.15",
  "org.webjars" % "jquery" % "2.1.1",
  "org.webjars" % "d3js" % "3.4.13"
)