import sbt._

object Dependencies {

  val production: Seq[ModuleID] = Seq(
    // Akka Persistence
    "com.typesafe.akka" %% "akka-persistence" % Version.akka,

    // Akka Persistence Cassandra
    "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.akkaPersistenceCassandra,

    // Akka Persistence JDBC
    "com.github.dnvriend" %% "akka-persistence-jdbc" % Version.akkaPersistenceJdbc,
    "org.postgresql" % "postgresql" % "42.2.5",

    // Akka Stream
    "com.typesafe.akka" %% "akka-stream" % Version.akka,

  )
  val test: Seq[ModuleID] = Seq(
    //"org.scalatest" %% "scalatest" % Versions.scala_test % Test,
    "org.scalatest" %% "scalatest" % Version.scalaTest,
    //"com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
    "com.typesafe.akka" %% "akka-testkit" % Version.akka,
    //"com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka % Test
    "com.typesafe.akka" %% "akka-stream-testkit" % Version.akka,
    
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.akkaPersistenceCassandra % Test
  )

  object Version {
    val akka = "2.5.22"
    val akkaPersistenceJdbc = "3.5.0"
    val akkaPersistenceCassandra = "0.94"
    val scalaTest = "3.0.7"
    val postgres = "42.2.5"
  }

}
