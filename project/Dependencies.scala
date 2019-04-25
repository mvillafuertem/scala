import sbt._

object Dependencies {

  val production: Seq[ModuleID] = Seq(
    // Akka Persistence
    "com.typesafe.akka" %% "akka-persistence" % Versions.akka,
    "com.postgresql" %% "postgresql" % "42.2.2",
    "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",
    
    // Akka Stream
    "com.typesafe.akka" %% "akka-stream" % Versions.akka,

  )
  val test: Seq[ModuleID] = Seq(
    //"org.scalatest" %% "scalatest" % Versions.scala_test % Test,
    "org.scalatest" %% "scalatest" % Versions.scala_test,
    //"com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
    "com.typesafe.akka" %% "akka-testkit" % Versions.akka,
    //"com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka % Test
    "com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka
  )

  object Versions {
    val akka = "2.5.22"
    val scala_test = "3.0.7"
  }

}
