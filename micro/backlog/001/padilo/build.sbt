organization := "net.pdiaz"

name := "cain-searcher"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
          "com.typesafe.akka" %% "akka-actor" % "2.3.3",
          "com.typesafe.akka" %% "akka-slf4j" % "2.3.3",
          "ch.qos.logback" % "logback-classic" % "1.0.10",
          "org.scala-lang.modules" %% "scala-async" % "0.9.1",
          "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test",
          "org.scalatest" %% "scalatest" % "2.2.0" % "test"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/release"  
)
