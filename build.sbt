name := """taxoscope"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws 
)

libraryDependencies ++= Seq(
  "nl.ebpi.tqa" % "tqa" % "0.5.5",
  "org.webjars" % "bootstrap" % "3.3.1",
  "org.webjars" % "d3js" % "3.4.13",
  "org.webjars" % "highlightjs" % "8.2-1",
  "org.webjars" % "typeaheadjs" % "0.10.5-1",
  "org.webjars" % "underscorejs" % "1.7.0",
  "org.webjars" % "requirejs" % "2.1.15",
  "org.webjars" % "jquery" % "2.1.1",
  "org.webjars" % "angularjs" % "1.3.8"
)