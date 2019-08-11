name := "scale-image"

version := "1.0"

lazy val `squarepreview` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.8"

val commonLibs: Seq[ModuleID] = Seq(
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8",

  "commons-io" % "commons-io" % "2.6",
  "commons-codec" % "commons-codec" % "1.13",

  "org.mockito" % "mockito-core" % "3.0.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0" % Test
)

val loggingLibs: Seq[ModuleID] = Seq(
  "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "ch.qos.logback" % "logback-core" % "1.1.7",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",
  "org.codehaus.janino" % "janino" % "3.0.7"
)

libraryDependencies ++=
  Seq( jdbc , ehcache , ws , guice ) ++
    commonLibs ++ loggingLibs