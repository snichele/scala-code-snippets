name := "springJdbcTemplate-snippet"

organization := "snichele"

version := "1"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2" % "1.7.1" % "test",
    "oracle" % "ojdbc14" % "10.2.0.2",
    "ch.qos.logback" % "logback-classic" % "0.9.29",
    "org.scalatest" %% "scalatest" % "1.6.1" % "test",
    "org.springframework" % "spring-context" % "3.1.1.RELEASE",
    "org.springframework" % "spring-jdbc" % "3.1.1.RELEASE",
    "org.springframework" % "spring-tx" % "3.1.1.RELEASE",
    "com.github.scala-incubator.io" %% "scala-io-core" % "0.3.0",
    "com.github.scala-incubator.io" %% "scala-io-file" % "0.3.0",
    "org.scalaz" %% "scalaz-core" % "6.0.4"
)

resolvers ++= Seq(
    "Scala Tools Repository" at "http://nexus.scala-tools.org/content/repositories/snapshots/",
    "FuseSource Repository" at "http://repo.fusesource.com/nexus/content/repositories/public",
    "Download java" at "http://download.java.net/maven/2/",
    "For oracle driver" at "http://mirrors.ibiblio.org/pub/mirrors/maven/mule/dependencies/maven2/"
)

//initialCommands := "import play2fuse.Play2Fuse._"
