name := "technical-tests"

organization := "snichele"

version := "1"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.7.1" % "test",
    "ch.qos.logback" % "logback-classic" % "0.9.29",
    "org.slf4j" % "slf4j-api" % "1.6.4",
    "org.scalaz" %% "scalaz-core" % "6.0.4"
)

resolvers ++= Seq(
    "Scala Tools Repository" at "http://nexus.scala-tools.org/content/repositories/snapshots/",
    "FuseSource Repository" at "http://repo.fusesource.com/nexus/content/repositories/public",
    "Download java" at "http://download.java.net/maven/2/"
)