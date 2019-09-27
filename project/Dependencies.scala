import Dependencies.Artifact
import sbt._

object Dependencies {

  val akka: Seq[ModuleID] =
    // A K K A
    Seq(
      Artifact.akkaPersistence,
      Artifact.akkaStream,
      Artifact.akkaSlf4f,
      Artifact.akkaActorTyped,
      Artifact.akkaStreamTyped,
      Artifact.akkaPersistenceTyped
    ).map(_ % Version.akka) ++ Seq(
      Artifact.logback % Version.logback,
      Artifact.akkaPersistenceCassandra % Version.akkaPersistenceCassandra,
      Artifact.akkaPersistenceJdbc % Version.akkaPersistenceJdbc,
      Artifact.postgresql % Version.postgres
    ) ++ Seq(
      // A K K A  T E S T
      Artifact.akkaTestKit,
      Artifact.akkaStreamTestkit,
      Artifact.akkaActorTestkitTyped
    ).map(_ % Version.akka) ++ Seq(
      Artifact.scalaTest % Version.scalaTest % "it,test",
      Artifact.akkaStreamKafkaTestkit % Version.akkaKafka % "it,test",
      Artifact.akkaPersistenceCassandraLauncher % Version.akkaPersistenceCassandra % Test,
      Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory % Test,
      Artifact.scalaTest % Version.scalaTest % Test
  )

  val advanced: Seq[ModuleID] = Seq(
    // A D V A N C E D  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val algorithms: Seq[ModuleID] = Seq(
    // A L G O R I T H M S  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val todo: Seq[ModuleID] =
    // T O D O
    Seq(
      Artifact.akkaPersistenceTyped % Version.akka,
      //"org.iq80.leveldb" % "leveldb" % "0.12",
      Artifact.leveldbjniAll % Version.leveldbjniAll,
      Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory,
      Artifact.logback % Version.logback
    ) ++ Seq(
      Artifact.tapirCore,
      Artifact.tapirAkkaHttpServer,
      Artifact.tapirJsonCirce,
      Artifact.tapirOpenapiDocs,
      Artifact.tapirOpenapiCirceYaml,
      Artifact.tapirSwaggerUiAkkaHttp
    ).map(_ % Version.tapir) ++ Seq(
      // T O D O  T E S T
      Artifact.akkaStreamTestkit % Version.akka,
      Artifact.akkaHttpTestkit % Version.akkaHttp,
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val cats: Seq[ModuleID] = Seq(
    // C A T S
    Artifact.catsCore,
    Artifact.catsFree
  ).map(_ % Version.cats)

  val circe: Seq[ModuleID] = Seq(
    // C I R C E
    Artifact.circeParser,
    Artifact.circeGeneric
  ).map(_ % Version.circe) ++ Seq(
    // C I R C E  T E S T
    Artifact.scalaTest % Version.scalaTest
  ).map(_ % Test)

  val slick: Seq[ModuleID] =
    // S L I C K
    Seq(
      Artifact.alpakkaSlick % Version.alpakkaSlick,
      Artifact.h2 % Version.h2
    ) ++ Seq(
      // S L I C K  T E S T
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val zio: Seq[ModuleID] =
    // Z I O
    Seq(
      Artifact.zio,
      Artifact.zioStreams
    ).map(_ % Version.zio) ++ Seq(
      // Z I O  T E S T
      Artifact.zioTest,
      Artifact.zioTestSbt
    ).map(_ % Version.zio % Test)

  private object Artifact {
    val akkaActorTestkitTyped = "com.typesafe.akka" %% "akka-actor-testkit-typed"
    val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed"
    val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit"
    val akkaPersistence = "com.typesafe.akka" %% "akka-persistence"
    val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra"
    val akkaPersistenceCassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher"
    val akkaPersistenceInmemory = "com.github.dnvriend" %% "akka-persistence-inmemory"
    val akkaPersistenceJdbc = "com.github.dnvriend" %% "akka-persistence-jdbc"
    val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed"
    val akkaSlf4f = "com.typesafe.akka" %% "akka-slf4j"
    val akkaStream = "com.typesafe.akka" %% "akka-stream"
    val akkaStreamKafkaTestkit = "com.typesafe.akka" %% "akka-stream-kafka-testkit"
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit"
    val akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed"
    val akkaTestKit = "com.typesafe.akka" %% "akka-testkit"
    val alpakkaSlick = "com.lightbend.akka" %% "akka-stream-alpakka-slick"
    val catsCore = "org.typelevel" %% "cats-core"
    val catsFree = "org.typelevel" %% "cats-free"
    val circeGeneric = "io.circe" %% "circe-generic"
    val circeParser = "io.circe" %% "circe-parser"
    val h2 = "com.h2database" % "h2"
    //"org.iq80.leveldb" % "leveldb" % "0.12",
    val leveldbjniAll = "org.fusesource.leveldbjni" % "leveldbjni-all"
    val logback = "ch.qos.logback" % "logback-classic"
    val postgresql = "org.postgresql" % "postgresql"
    val scalaTest = "org.scalatest" %% "scalatest"
    val tapirAkkaHttpServer = "com.softwaremill.tapir" %% "tapir-akka-http-server"
    val tapirCore = "com.softwaremill.tapir" %% "tapir-core"
    val tapirJsonCirce = "com.softwaremill.tapir" %% "tapir-json-circe"
    val tapirOpenapiCirceYaml = "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml"
    val tapirOpenapiDocs = "com.softwaremill.tapir" %% "tapir-openapi-docs"
    val tapirSwaggerUiAkkaHttp = "com.softwaremill.tapir" %% "tapir-swagger-ui-akka-http"
    val zio = "dev.zio" %% "zio"
    val zioStreams = "dev.zio" %% "zio-streams"
    val zioTest = "dev.zio" %% "zio-test"
    val zioTestSbt = "dev.zio" %% "zio-test-sbt"
  }

  private object Version {
    val akka = "2.5.25"
    val akkaHttp = "10.1.10"
    val akkaKafka = "1.0.5"
    val akkaPersistenceCassandra = "0.99"
    val akkaPersistenceInmemory = "2.5.15.2"
    val akkaPersistenceJdbc = "3.5.2"
    val alpakkaSlick = "1.1.1"
    val cats = "2.0.0"
    val circe = "0.12.1"
    val h2 = "1.4.199"
    val leveldbjniAll = "1.8"
    val logback = "1.2.3"
    val postgres = "42.2.8"
    val scalaTest = "3.0.8"
    val tapir = "0.11.3"
    val zio = "1.0.0-RC13"
  }

}
