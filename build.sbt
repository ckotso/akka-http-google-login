lazy val VERSION = "0.1.0"

organization := "gr.costas"

version := VERSION

scalaVersion := "2.11.8"

crossPaths := false

logBuffered := false

parallelExecution in Test := false

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= {
  lazy val akkaVersion = "2.4.11"
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test",
    "de.heikoseeberger" %% "akka-http-argonaut" % "1.10.0"
  )
}

enablePlugins(SbtNativePackager)
