name := "SquarePreview"

version := "1.0"

lazy val `squarepreview` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

val imageLibs: Seq[ModuleID] = Seq(
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8"
)

val loggingLibs: Seq[ModuleID] = Seq(
  "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "ch.qos.logback" % "logback-core" % "1.1.7",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",
  "org.codehaus.janino" % "janino" % "3.0.7"
)

val playLibs: Seq[ModuleID] = Seq(
  "com.typesafe.play" %% "play-json" % "2.7.3"
)

val webjars: Seq[ModuleID] = Seq(
  "org.webjars" %% "webjars-play" % "2.7.3",
  "org.webjars" % "knockout" % "3.3.0",
  "org.webjars" % "jquery" % "1.11.3",
  "org.webjars" % "jquery-ui" % "1.11.4",
  "org.webjars" % "jquery-ui-src" % "1.11.4",
  "org.webjars" % "jquery-file-upload" % "9.10.1",
  "org.webjars" % "bootstrap" % "3.3.4"
)

libraryDependencies ++=
  Seq( jdbc , ehcache , ws , specs2 % Test , guice ) ++
    imageLibs ++ playLibs ++ loggingLibs ++ webjars 

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )