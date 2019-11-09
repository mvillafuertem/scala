import sbt._

object Dependencies {

  val `akka-untyped`: Seq[ModuleID] =
    // A K K A
    Seq(
      Artifact.akkaPersistence,
      Artifact.akkaSlf4f,
      Artifact.akkaStream
    ).map(_ % Version.akka) ++ Seq(
      //Artifact.akkaPersistenceCassandra % Version.akkaPersistenceCassandra,
      //Artifact.akkaPersistenceJdbc % Version.akkaPersistenceJdbc,
      Artifact.logback % Version.logback,
      Artifact.postgresql % Version.postgres
    ) ++ Seq(
      // A K K A  T E S T
      Artifact.akkaStreamTestkit,
      Artifact.akkaTestKit
    ).map(_ % Version.akka) ++ Seq(
      //Artifact.akkaPersistenceCassandraLauncher % Version.akkaPersistenceCassandra % Test,
      //Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory % Test,
      Artifact.scalaTest % Version.scalaTest % "it,test"
  )

  val `akka-typed`: Seq[ModuleID] =
    // A K K A  T Y P E D
    Seq(
      Artifact.akkaActorTyped,
      Artifact.akkaPersistenceTyped,
      Artifact.akkaSlf4f,
      Artifact.akkaStreamTyped
    ).map(_ % Version.akka) ++ Seq(
      Artifact.logback % Version.logback
    ) ++ Seq(
      // A K K A  T Y P E D  T E S T
      Artifact.akkaActorTestkitTyped,
      Artifact.akkaStreamTestkit
    ).map(_ % Version.akka) ++ Seq(
      //Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory % Test,
      Artifact.scalaTest % Version.scalaTest % "it,test"
  )

  val alpakka: Seq[ModuleID] =
    // A L P A K K A
    Seq(
      Artifact.akkaSlf4f
    ).map(_ % Version.akka) ++ Seq(
      Artifact.logback % Version.logback
    ) ++ Seq(
      // A L P A K K A  T E S T
      Artifact.akkaStreamKafkaTestkit % Version.akkaKafka % "it,test",
      Artifact.testcontainers % Version.testcontainers % "it,test",
      Artifact.testcontainersKafka % Version.testcontainersKafka % "it,test",
      Artifact.scalaTest % Version.scalaTest % "it,test"
  )

  val advanced: Seq[ModuleID] = Seq(
    // A D V A N C E D  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val algorithms: Seq[ModuleID] = Seq(
    // A L G O R I T H M S  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val basic: Seq[ModuleID] = Seq(
    // B A S I C  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val todo: Seq[ModuleID] =
    // T O D O
    Seq(
      Artifact.akkaPersistenceTyped % Version.akka,
      //"org.iq80.leveldb" % "leveldb" % "0.12",
      Artifact.leveldbjniAll % Version.leveldbjniAll,
      //Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory,
      Artifact.logback % Version.logback
    ) ++ Seq(
      Artifact.tapirCore,
      Artifact.tapirAkkaHttpServer,
      Artifact.tapirJsonCirce,
      Artifact.tapirOpenapiCirceYaml,
      Artifact.tapirOpenapiDocs,
      Artifact.tapirSwaggerUiAkkaHttp
    ).map(_ % Version.tapir) ++ Seq(
      // T O D O  T E S T
      Artifact.akkaActorTestkitTyped % Version.akka,
      Artifact.akkaStreamTestkit % Version.akka,
      Artifact.akkaHttpTestkit % Version.akkaHttp,
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val products: Seq[ModuleID] =
  // P R O D U C T S
    Seq(
      //"org.iq80.leveldb" % "leveldb" % "0.12",
      Artifact.zio % Version.zio,
      Artifact.zioInteropReactiveStreams % Version.zioInteropReactiveStreams,
      Artifact.slick % Version.slick,
      Artifact.h2 % Version.h2,
      Artifact.logback % Version.logback
    ) ++ Seq(
      Artifact.tapirCore,
      Artifact.tapirAkkaHttpServer,
      Artifact.tapirJsonCirce,
      Artifact.tapirOpenapiCirceYaml,
      Artifact.tapirOpenapiDocs,
      Artifact.tapirSwaggerUiAkkaHttp
    ).map(_ % Version.tapir) ++ Seq(
      // P R O D U C T S  T E S T
      Artifact.akkaHttpTestkit % Version.akkaHttp,
      Artifact.scalaTest % Version.scalaTest,
      Artifact.zioTest % Version.zio
    ).map(_ % Test)

  val cats: Seq[ModuleID] = Seq(
    // C A T S
    Artifact.catsCore,
    Artifact.catsFree
  ).map(_ % Version.cats) ++ Seq(
    // C A T S  T E S T
    Artifact.scalaTest % Version.scalaTest
  ).map(_ % Test)

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
    ).map(_ % Version.zio % Test) ++ Seq(
      // S P E C  T E S T
      Artifact.specs2Core,
      Artifact.specs2MatcherExtra
    ).map(_ % Version.specs2 % Test) ++ Seq(
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  private object Artifact {
    val akkaActorTestkitTyped = "com.typesafe.akka" %% "akka-actor-testkit-typed"
    val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed"
    val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit"
    val akkaPersistence = "com.typesafe.akka" %% "akka-persistence"
    //val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra"
    //val akkaPersistenceCassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher"
    //val akkaPersistenceInmemory = "com.github.dnvriend" %% "akka-persistence-inmemory"
    //val akkaPersistenceJdbc = "com.github.dnvriend" %% "akka-persistence-jdbc"
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
    val slick = "com.typesafe.slick" %% "slick"
    val specs2Core = "org.specs2" %% "specs2-core"
    val specs2MatcherExtra = "org.specs2" %% "specs2-matcher-extra"
    val tapirAkkaHttpServer = "com.softwaremill.tapir" %% "tapir-akka-http-server"
    val tapirCore = "com.softwaremill.tapir" %% "tapir-core"
    val tapirJsonCirce = "com.softwaremill.tapir" %% "tapir-json-circe"
    val tapirOpenapiCirceYaml = "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml"
    val tapirOpenapiDocs = "com.softwaremill.tapir" %% "tapir-openapi-docs"
    val tapirSwaggerUiAkkaHttp = "com.softwaremill.tapir" %% "tapir-swagger-ui-akka-http"
    val testcontainers = "com.dimafeng" %% "testcontainers-scala"
    val testcontainersKafka = "org.testcontainers" % "kafka"
    val zio = "dev.zio" %% "zio"
    val zioInteropReactiveStreams = "dev.zio" %% "zio-interop-reactivestreams"
    val zioStreams = "dev.zio" %% "zio-streams"
    val zioTest = "dev.zio" %% "zio-test"
    val zioTestSbt = "dev.zio" %% "zio-test-sbt"
  }

  private object Version {
    val akka = "2.6.0"
    val akkaHttp = "10.1.10"
    val akkaKafka = "1.1.0"
    val akkaPersistenceCassandra = "0.100"
    val akkaPersistenceInmemory = "2.5.15.2"
    val akkaPersistenceJdbc = "3.5.2"
    val alpakkaSlick = "1.1.2"
    val cats = "2.0.0"
    val circe = "0.12.1"
    val h2 = "1.4.200"
    val leveldbjniAll = "1.8"
    val logback = "1.2.3"
    val postgres = "42.2.8"
    val scalaTest = "3.0.8"
    val specs2 = "4.6.0"
    val slick = "3.3.2"
    val tapir = "0.11.9"
    val testcontainers = "0.33.0"
    val testcontainersKafka = "1.12.3"
    val zio = "1.0.0-RC16"
    val zioInteropReactiveStreams = "1.0.3.4-RC1"
  }

}
