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

libraryDependencies += "nl.ebpi.tqa" % "tqa" % "0.5.1"
