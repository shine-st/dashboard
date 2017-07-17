name := """dashboard"""
organization := "shine.st"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  guice,
  ehcache,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
)

libraryDependencies += "org.mariadb.jdbc" % "mariadb-java-client" % "2.0.1"

libraryDependencies += "shine.st" %% "common" % "2.0.1"




// Adds additional packages into Twirl
//TwirlKeys.templateImports += "shine.st.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "shine.st.binders._"
