organization := "net.pdiaz"

name := "flyapp"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.7.1",
  "net.databinder" %% "unfiltered-netty" % "0.7.1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
  "io.argonaut" %% "argonaut" % "6.0.1" ,
  "io.argonaut" %% "argonaut-unfiltered" % "6.0",
  "com.github.nscala-time" %% "nscala-time" % "0.6.0",
  "net.databinder" %% "unfiltered-spec" % "0.7.1" % "test"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/release",
  "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
)
