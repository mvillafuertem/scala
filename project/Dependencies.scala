import sbt._

object Dependencies {
  
  val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-persistence",
    "com.typesafe.akka" %% "akka-stream"
  ).map(_ % Version.akka) ++ Seq(
    "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.akkaPersistenceCassandra,
    "com.github.dnvriend" %% "akka-persistence-jdbc" % Version.akkaPersistenceJdbc,
    "org.postgresql" % "postgresql" % "42.2.5"
  )
  
  val akkaTest: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-testkit",
    "com.typesafe.akka" %% "akka-stream-testkit",
  ).map(_ % Version.akka) ++ Seq(
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.akkaPersistenceCassandra % Test
  )

  val cats: Seq[ModuleID] = Seq(
    // Cats
    "org.typelevel" %% "cats-core",
    "org.typelevel" %% "cats-free"
  ).map(_ % Version.cats)

  val test: Seq[ModuleID] = Seq(
    //"org.scalatest" %% "scalatest" % Versions.scala_test % Test,
    "org.scalatest" %% "scalatest" % Version.scalaTest,
  )

  object Version {
    val akka = "2.5.22"
    val akkaPersistenceJdbc = "3.5.0"
    val akkaPersistenceCassandra = "0.94"
    val scalaTest = "3.0.7"
    val postgres = "42.2.5"
    val cats = "2.0.0-M1"
  }

}
