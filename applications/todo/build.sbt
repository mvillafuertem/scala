name := "todo"

version := "0.1"

scalaVersion := "2.13.0"


libraryDependencies += "com.typesafe.akka" %% "akka-persistence-typed" % "2.6.0-M6"
libraryDependencies += "com.softwaremill.tapir" %% "tapir-core" % "0.9.3"
libraryDependencies += "com.softwaremill.tapir" %% "tapir-akka-http-server" % "0.9.3"

//libraryDependencies += "org.iq80.leveldb" % "leveldb" % "0.12"
libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
libraryDependencies += "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.15.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test




