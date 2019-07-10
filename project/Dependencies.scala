import sbt._

object Dependencies {
  
  val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-persistence",
    "com.typesafe.akka" %% "akka-stream",
    "com.typesafe.akka" %% "akka-slf4j",
    
    // T Y P E D
    "com.typesafe.akka" %% "akka-actor-typed",
    "com.typesafe.akka" %% "akka-stream-typed",
    "com.typesafe.akka" %% "akka-persistence-typed",
  ).map(_ % Version.akka) ++ Seq(
    // L O G B A C K
    "ch.qos.logback" % "logback-classic" % Version.logback,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.akkaPersistenceCassandra,
    "com.github.dnvriend" %% "akka-persistence-jdbc" % Version.akkaPersistenceJdbc,
    "org.postgresql" % "postgresql" % "42.2.5"
  )
  
  val akkaTest: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-testkit",
    "com.typesafe.akka" %% "akka-stream-testkit",
    
    // T Y P E D
    "com.typesafe.akka" %% "akka-actor-testkit-typed",
  ).map(_ % Version.akka) ++ Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest % "it,test",
    "com.typesafe.akka" %% "akka-stream-kafka-testkit" % Version.akkaKafka % "it,test",
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.akkaPersistenceCassandra % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.15.1" % Test,
  )

  val cats: Seq[ModuleID] = Seq(
    // C A T S
    "org.typelevel" %% "cats-core",
    "org.typelevel" %% "cats-free"
  ).map(_ % Version.cats)

  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest,
  )

  object Version {
    val akka = "2.5.23"
    val akkaKafka = "1.0.4"
    val akkaPersistenceJdbc = "3.5.0"
    val akkaPersistenceCassandra = "0.94"
    val scalaTest = "3.0.8"
    val postgres = "42.2.5"
    val cats = "2.0.0-M1"
    val logback = "1.2.3"
  }

}
