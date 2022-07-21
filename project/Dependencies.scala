import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys.libraryDependencies
import sbt.{ Def, _ }

object Dependencies {

  val `akka-classic`: Seq[ModuleID] =
    // A K K A
    Seq(
      Artifact.akkaPersistence,
      Artifact.akkaSlf4f,
      Artifact.akkaStream
    ).map(_ % Version.akka) ++ Seq(
      // Artifact.akkaPersistenceCassandra % Version.akkaPersistenceCassandra,
      // Artifact.akkaPersistenceJdbc % Version.akkaPersistenceJdbc,
      Artifact.logback    % Version.logback,
      Artifact.postgresql % Version.postgres
    ) ++ Seq(
      // A K K A  T E S T
      Artifact.akkaStreamTestkit,
      Artifact.akkaTestKit
    ).map(_ % Version.akka) ++ Seq(
      // Artifact.akkaPersistenceCassandraLauncher % Version.akkaPersistenceCassandra % Test,
      // Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory % Test,
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
      // Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory % Test,
      Artifact.scalaTest % Version.scalaTest % "it,test"
    )

  val `alpakka-kafka`: Seq[ModuleID] =
    // A L P A K K A
    Seq(
      // A L P A K K A  I N T E G R A T I O N  T E S T
      Artifact.akkaSlf4f              % Version.akka,
      Artifact.akkaStreamKafkaTestkit % Version.alpakkaKafka,
      Artifact.logback                % Version.logback,
      Artifact.scalaTest              % Version.scalaTest,
      Artifact.testcontainersCore     % Version.testcontainers,
      Artifact.testcontainersKafka    % Version.testcontainersKafka
    ).map(_ % IntegrationTest)

  val `alpakka-mongodb`: Seq[ModuleID] =
    // A L P A K K A
    Seq(
      // A L P A K K A  I N T E G R A T I O N  T E S T
      Artifact.mongoScalaBson     % Version.mongoScalaBson,
      Artifact.akkaStreamTestkit  % Version.akka,
      Artifact.alpakkaMongodb     % Version.alpakkaMongodb,
      Artifact.akkaSlf4f          % Version.akka,
      Artifact.logback            % Version.logback,
      Artifact.scalaTest          % Version.scalaTest,
      Artifact.testcontainersCore % Version.testcontainers
    ).map(_ % IntegrationTest)

  val advanced: Seq[ModuleID] = Seq(
    // A D V A N C E D  T E S T
    Artifact.scalaTest        % Version.scalaTest          % Test,
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
  )

  val algorithms: Seq[ModuleID] = Seq(
    // A L G O R I T H M S  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val `aws-cdk`: Seq[ModuleID] = Seq(
    // "com.hashicorp" % "cdktf" % "0.0.17",
    // "software.constructs" % "constructs" % "3.0.0"
  ) ++ Seq(
    // A W S
    Artifact.awsCdkEcsPatterns
  ).map(_ % Version.awsCdk)

  val `aws-sdk`: Seq[ModuleID] = Seq(
    // A W S
    Artifact.awsLambda % Version.awsLambda
  ) ++ Seq(
    Artifact.circeGeneric,
    Artifact.circeGenericExtras,
    Artifact.circeParser
  ).map(_ % Version.circe) ++ (Seq(
    // A W S  I N T E G R A T I O N  T E S T
    Artifact.awsSdkCloudWatch,
    Artifact.awsSdkLambda,
    Artifact.awsSdkS3
  ).map(_ % Version.awsSdk) ++ Seq(
    Artifact.java8Compat        % Version.java8Compat,
    Artifact.logback            % Version.logback,
    Artifact.scalaTest          % Version.scalaTest,
    Artifact.testcontainersCore % Version.testcontainers
  )).map(_ % IntegrationTest)

  val basic: Seq[ModuleID] = Seq(
    // B A S I C  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val cask: Seq[ModuleID] = Seq(
    // C A S K
    Artifact.cask      % Version.cask
  ) ++ Seq(
    // C A S K  T E S T
    Artifact.scalaTest % Version.scalaTest % Test
  )

  val `akka-fsm`: Seq[ModuleID] =
    // A K K A  F S M
    Seq(
      Artifact.akkaPersistenceTyped % Version.akka,
      // "org.iq80.leveldb" % "leveldb" % "0.12",
      Artifact.leveldbjniAll        % Version.leveldbjniAll,
      // Artifact.akkaPersistenceInmemory % Version.akkaPersistenceInmemory,
      Artifact.logback              % Version.logback
    ) ++ Seq(
      Artifact.tapirCore,
      Artifact.tapirAkkaHttpServer,
      Artifact.tapirJsonCirce,
      Artifact.tapirOpenapiCirceYaml,
      Artifact.tapirOpenapiDocs,
      Artifact.tapirSwaggerUiBundle
    ).map(_ % Version.tapir) ++ Seq(
      // A K K A  F S M  T E S T
      Artifact.akkaActorTestkitTyped % Version.akka,
      Artifact.akkaStreamTestkit     % Version.akka,
      Artifact.akkaHttpTestkit       % Version.akkaHttp,
      Artifact.scalaTest             % Version.scalaTest
    ).map(_ % Test)

  val `akka-http`: Seq[ModuleID] =
    // A K K A  H T T P
    Seq(
      Artifact.logback       % Version.logback,
      Artifact.jwtCirce      % Version.jwtCirce,
      Artifact.akkaHttpCirce % Version.akkaHttpCirce
    ) ++ Seq(
      Artifact.tapirCore,
      Artifact.tapirAkkaHttpServer,
      Artifact.tapirJsonCirce,
      Artifact.tapirOpenapiCirceYaml,
      Artifact.tapirOpenapiDocs,
      Artifact.tapirSwaggerUiBundle
    ).map(_ % Version.tapir) ++ Seq(
      // A K K A  H T T P  T E S T
      Artifact.akkaHttpTestkit % Version.akkaHttp,
      Artifact.akkaTestKit     % Version.akka,
      Artifact.scalaTest       % Version.scalaTest
    ).map(_ % Test)

  val `sensor-controller`: Seq[ModuleID] =
    // S E N S O R  C O N T R O L L E R
    Seq(
      Artifact.akkaActorTyped,
      Artifact.akkaStreamTyped
    ).map(_ % Version.akka) ++ Seq(
      Artifact.alpakkaKafka % Version.alpakkaKafka,
      Artifact.ficus        % Version.ficus,
      Artifact.curator      % Version.curator,
      Artifact.kafka        % Version.kafka,
      Artifact.logback      % Version.logback
    ) ++ Seq(
      // S E N S O R  C O N T R O L L E R  T E S T
      Artifact.akkaHttpTestkit % Version.akkaHttp,
      Artifact.scalaTest       % Version.scalaTest,
      Artifact.zioTest         % Version.zio
    ).map(_ % Test)

  val cats: Seq[ModuleID] = Seq(
    // C A T S
    Artifact.catsCore,
    Artifact.catsFree
  ).map(_ % Version.cats) ++ Seq(
    // C A T S  T E S T
    Artifact.scalaTest % Version.scalaTest
  ).map(_ % Test)

  val docs: Def.Setting[Seq[ModuleID]] = libraryDependencies ++= Seq(
    // P R O D U C T I O N
    "me.shadaj" %%% "slinky-hot",
    "me.shadaj" %%% "slinky-react-router"
  ).map(_ % Version.slinky) ++ Seq(
    "dev.zio"           %%% "zio"             % Version.zio,
    "io.github.cquiroz" %%% "scala-java-time" % Version.scalaJavaTime
  )

  val dotty: Seq[ModuleID] = Seq(
    // D O T T Y
    Artifact.scalaTest % Version.scalaTest
  )

  val foundations: Seq[ModuleID] = Seq(
    // D O T T Y
    Artifact.scalaCheck % Version.scalaCheck,
    Artifact.scalaTest  % Version.scalaTest
  ).map(_ % Test)

  val `graalvm-cli`: Seq[ModuleID] = Seq(
    // G R A A L  V M
    Artifact.picocli        % Version.picocli,
    Artifact.picocliCodegen % Version.picocli % Provided
  )

  val http4s: Seq[ModuleID] = Seq(
    // H T T P 4 S
    Artifact.http4sBlazeServer % Version.http4s,
    Artifact.http4sDsl         % Version.http4s,
    Artifact.zio               % Version.zio,
    Artifact.zioInteropCats    % Version.zioInteropCats
  )

  val json: Seq[ModuleID] = Seq(
    // J S O N
    Artifact.circeGeneric,
    Artifact.circeGenericExtras,
    Artifact.circeOptics,
    Artifact.circeParser
  ).map(_ % Version.circe) ++ Seq(
    Artifact.jsoniterCore,
    Artifact.jsoniterMacros
  ).map(_ % Version.jsoniter) ++ Seq(
    Artifact.dijon % Version.dijon,
    Artifact.jslt  % Version.jslt
  ) ++ Seq(
    // J S O N  T E S T
    Artifact.scalaTest % Version.scalaTest
  ).map(_ % Test)

  val reflection: Seq[ModuleID] =
    Seq(
      // R E F L E C T I O N  T E S T
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val script: Seq[ModuleID] =
    Seq(
      // S C R I P T  T E S T
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val slick: Seq[ModuleID] =
    // S L I C K
    Seq(
      Artifact.alpakkaSlick % Version.alpakkaSlick,
      Artifact.h2           % Version.h2
    ) ++ Seq(
      // S L I C K  T E S T
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val slinky: Def.Setting[Seq[ModuleID]] = libraryDependencies ++= Seq(
    // P R O D U C T I O N
    "dev.zio"           %%% "zio"             % Version.zio,
    "me.shadaj"         %%% "slinky-hot"      % Version.slinky,
    "io.github.cquiroz" %%% "scala-java-time" % Version.scalaJavaTime
  )

  val spark: Seq[ModuleID] =
    // S P A R K
    Seq(
      Artifact.sparkCore,
      Artifact.sparkSql,
      Artifact.sparkStreaming
    ).map(_ % Version.spark) ++ Seq(
      // S P A R K  T E S T
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val sttp: Seq[ModuleID] =
    // S T T P
    Seq(
      Artifact.sttpAsyncAkka,
      Artifact.sttpAsyncZioStreams,
      Artifact.sttpCore,
      Artifact.sttpCirce
    ).map(_ % Version.sttp) ++ Seq(
      Artifact.tapirCore,
      Artifact.tapirJsonCirce,
      Artifact.tapirSttpClient
    ).map(_ % Version.tapir) ++ Seq(
      Artifact.circeGeneric,
      Artifact.circeGenericExtras
    ).map(_ % Version.circe) ++ Seq(
      Artifact.akkaStream % Version.akka
    ) ++ Seq(
      // S T T P  T E S T
      Artifact.zioTest,
      Artifact.zioTestSbt
    ).map(_ % "2.0.0-RC2" % Test) ++ Seq(
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  val tapir: Seq[ModuleID] =
    // T A P I R
    Seq(
      Artifact.akkaActorTyped,
      Artifact.akkaStreamTyped
    ).map(_ % Version.akka) ++ Seq(
      // "org.iq80.leveldb" % "leveldb" % "0.12",
      "dev.zio"                         %% "zio-config"          % "3.0.0-RC6",
      "dev.zio"                         %% "zio-config-typesafe" % "3.0.0-RC6",
      Artifact.h2                        % Version.h2,
      Artifact.logback                   % Version.logback,
      Artifact.slick                     % Version.slick,
      Artifact.zio                       % "2.0.0-RC3",
      Artifact.zioInteropReactiveStreams % Version.zioInteropReactiveStreams,
      Artifact.zioLoggingSlf4j           % Version.zioLoggingSlf4j
    ) ++ Seq(
      Artifact.tapirCore,
      Artifact.tapirAkkaHttpServer,
      Artifact.tapirJsonCirce,
      Artifact.tapirOpenapiCirceYaml,
      Artifact.tapirOpenapiDocs,
      Artifact.tapirSwaggerUiBundle
    ).map(_ % Version.tapir) ++ Seq(
      // T A P I R  T E S T
      Artifact.akkaHttpTestkit % Version.akkaHttp,
      Artifact.scalaTest       % Version.scalaTest,
      Artifact.zioTest         % "2.0.0-RC2"
    ).map(_ % Test)

  val `terraform-cdktf-scalajs`: Def.Setting[Seq[ModuleID]] = libraryDependencies ++= Seq(
    // T E R R A F O R M  C D K T F
    "io.circe" %%% "circe-generic",
    "io.circe" %%% "circe-optics",
    "io.circe" %%% "circe-parser"
  ).map(_ % Version.circe) ++ Seq(
    // T E R R A F O R M  C D K T F  T E S T
    "org.scalatest" %%% "scalatest" % Version.scalaTest % Test
  )

  val `terraform-cdktf-scala`: Seq[ModuleID] = Seq(
    // T E R R A F O R M  C D K T F
    "com.hashicorp"  % "cdktf"     % "0.9.4"
    // "software.constructs" % "constructs" % "10.0.9"
  ) ++ Seq(
    // T E R R A F O R M  C D K T F  T E S T
    "org.scalatest" %% "scalatest" % Version.scalaTest % Test
  )

  val zio: Seq[ModuleID] =
    // Z I O
    Seq(
      Artifact.zio,
      Artifact.zioStreams
    ).map(_ % Version.zio) ++ Seq(
      Artifact.zioAkkaCluster % Version.zioAkkaCluster,
      Artifact.zioKafka       % Version.zioKafka
    ) ++ Seq(
      // Z I O  T E S T
      Artifact.zioTest,
      Artifact.zioTestSbt
    ).map(_ % Version.zio % Test) ++ Seq(
      Artifact.scalaTest % Version.scalaTest
    ).map(_ % Test)

  private object Artifact {
    // "org.iq80.leveldb" % "leveldb" % "0.12",
    // val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra"
    // val akkaPersistenceCassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher"
    // val akkaPersistenceInmemory = "com.github.dnvriend" %% "akka-persistence-inmemory"
    // val akkaPersistenceJdbc = "com.github.dnvriend" %% "akka-persistence-jdbc"
    val akkaActorTestkitTyped     = "com.typesafe.akka"                     %% "akka-actor-testkit-typed"
    val akkaActorTyped            = "com.typesafe.akka"                     %% "akka-actor-typed"
    val akkaHttp                  = "com.typesafe.akka"                     %% "akka-http"
    val akkaHttpCirce             = "de.heikoseeberger"                     %% "akka-http-circe"
    val akkaHttpTestkit           = "com.typesafe.akka"                     %% "akka-http-testkit"
    val akkaPersistence           = "com.typesafe.akka"                     %% "akka-persistence"
    val akkaPersistenceTyped      = "com.typesafe.akka"                     %% "akka-persistence-typed"
    val akkaSlf4f                 = "com.typesafe.akka"                     %% "akka-slf4j"
    val akkaStream                = "com.typesafe.akka"                     %% "akka-stream"
    val akkaStreamKafkaTestkit    = "com.typesafe.akka"                     %% "akka-stream-kafka-testkit"
    val akkaStreamTestkit         = "com.typesafe.akka"                     %% "akka-stream-testkit"
    val akkaStreamTyped           = "com.typesafe.akka"                     %% "akka-stream-typed"
    val akkaTestKit               = "com.typesafe.akka"                     %% "akka-testkit"
    val alpakkaKafka              = "com.typesafe.akka"                     %% "akka-stream-kafka"
    val alpakkaMongodb            = "com.lightbend.akka"                    %% "akka-stream-alpakka-mongodb"
    val alpakkaSlick              = "com.lightbend.akka"                    %% "akka-stream-alpakka-slick"
    val awsCdkEcsPatterns         = "software.amazon.awscdk"                 % "ecs-patterns"
    val awsLambda                 = "com.amazonaws"                          % "aws-lambda-java-core"
    val awsSdkCloudWatch          = "software.amazon.awssdk"                 % "cloudwatch"
    val awsSdkLambda              = "software.amazon.awssdk"                 % "lambda"
    val awsSdkS3                  = "software.amazon.awssdk"                 % "s3"
    val cask                      = "com.lihaoyi"                           %% "cask"
    val catsCore                  = "org.typelevel"                         %% "cats-core"
    val catsFree                  = "org.typelevel"                         %% "cats-free"
    val circeGeneric              = "io.circe"                              %% "circe-generic"
    val circeGenericExtras        = "io.circe"                              %% "circe-generic-extras"
    val circeOptics               = "io.circe"                              %% "circe-optics"
    val circeParser               = "io.circe"                              %% "circe-parser"
    val curator                   = "org.apache.curator"                     % "curator-test"
    val dijon                     = "me.vican.jorge"                        %% "dijon"
    val ficus                     = "com.iheart"                            %% "ficus"
    val h2                        = "com.h2database"                         % "h2"
    val http4sBlazeServer         = "org.http4s"                            %% "http4s-blaze-server"
    val http4sDsl                 = "org.http4s"                            %% "http4s-dsl"
    val java8Compat               = "org.scala-lang.modules"                %% "scala-java8-compat"
    val jslt                      = "com.schibsted.spt.data"                 % "jslt"
    val jsoniterCore              = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"
    val jsoniterMacros            = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"
    val jwtCirce                  = "com.pauldijou"                         %% "jwt-circe"
    val kafka                     = "org.apache.kafka"                      %% "kafka"
    val leveldbjniAll             = "org.fusesource.leveldbjni"              % "leveldbjni-all"
    val logback                   = "ch.qos.logback"                         % "logback-classic"
    val mongoScalaBson            = "org.mongodb.scala"                     %% "mongo-scala-bson"
    val picocli                   = "info.picocli"                           % "picocli"
    val picocliCodegen            = "info.picocli"                           % "picocli-codegen"
    val postgresql                = "org.postgresql"                         % "postgresql"
    val scalaCheck                = "org.scalatestplus"                     %% "scalacheck-1-14"
    val scalaTest                 = "org.scalatest"                         %% "scalatest"
    val slick                     = "com.typesafe.slick"                    %% "slick"
    val sparkCore                 = "org.apache.spark"                      %% "spark-core"
    val sparkSql                  = "org.apache.spark"                      %% "spark-sql"
    val sparkStreaming            = "org.apache.spark"                      %% "spark-streaming"
    val sttpAsyncAkka             = "com.softwaremill.sttp.client3"         %% "akka-http-backend"
    val sttpAsyncZioStreams       = "com.softwaremill.sttp.client3"         %% "async-http-client-backend-zio"
    val sttpCirce                 = "com.softwaremill.sttp.client3"         %% "circe"
    val sttpCore                  = "com.softwaremill.sttp.client3"         %% "core"
    val tapirAkkaHttpServer       = "com.softwaremill.sttp.tapir"           %% "tapir-akka-http-server"
    val tapirCore                 = "com.softwaremill.sttp.tapir"           %% "tapir-core"
    val tapirJsonCirce            = "com.softwaremill.sttp.tapir"           %% "tapir-json-circe"
    val tapirOpenapiCirceYaml     = "com.softwaremill.sttp.tapir"           %% "tapir-openapi-circe-yaml"
    val tapirOpenapiDocs          = "com.softwaremill.sttp.tapir"           %% "tapir-openapi-docs"
    val tapirSttpClient           = "com.softwaremill.sttp.tapir"           %% "tapir-sttp-client"
    val tapirSwaggerUiBundle      = "com.softwaremill.sttp.tapir"           %% "tapir-swagger-ui-bundle"
    val testcontainersCore        = "com.dimafeng"                          %% "testcontainers-scala-core"
    val testcontainersKafka       = "org.testcontainers"                     % "kafka"
    val zio                       = "dev.zio"                               %% "zio"
    val zioAkkaCluster            = "dev.zio"                               %% "zio-akka-cluster"
    val zioInteropCats            = "dev.zio"                               %% "zio-interop-cats"
    val zioInteropReactiveStreams = "dev.zio"                               %% "zio-interop-reactivestreams"
    val zioKafka                  = "dev.zio"                               %% "zio-kafka"
    val zioLoggingSlf4j           = "dev.zio"                               %% "zio-logging-slf4j"
    val zioStreams                = "dev.zio"                               %% "zio-streams"
    val zioTest                   = "dev.zio"                               %% "zio-test"
    val zioTestSbt                = "dev.zio"                               %% "zio-test-sbt"
  }

  private object Version {
    val akka                      = "2.6.18"
    val akkaHttp                  = "10.2.9"
    val akkaHttpCirce             = "1.39.2"
    val akkaPersistenceCassandra  = "0.100"
    val akkaPersistenceInmemory   = "2.5.15.2"
    val akkaPersistenceJdbc       = "3.5.2"
    val alpakkaKafka              = "3.0.0"
    val alpakkaMongodb            = "3.0.4"
    val alpakkaSlick              = "3.0.4"
    val awsCdk                    = "1.156.0"
    val awsLambda                 = "1.2.1"
    val awsSdk                    = "2.17.234"
    val cask                      = "0.7.11"
    val cats                      = "2.8.0"
    val circe                     = "0.14.1"
    val http4s                    = "1.0.0-M34"
    val curator                   = "5.3.0"
    val dijon                     = "0.6.0"
    val ficus                     = "1.5.2"
    val h2                        = "2.1.214"
    val java8Compat               = "1.0.2"
    val jslt                      = "0.1.12"
    val jsoniter                  = "2.13.36"
    val jwtCirce                  = "4.3.0"
    val kafka                     = "3.2.0"
    val leveldbjniAll             = "1.8"
    val logback                   = "1.2.11"
    val mongoScalaBson            = "4.4.2"
    val picocli                   = "4.6.1"
    val postgres                  = "42.4.0"
    val scalaCheck                = "3.2.2.0"
    val scalaJavaTime             = "2.0.0"
    val scalaTest                 = "3.2.12"
    val slick                     = "3.3.3"
    val slinky                    = "0.7.2"
    val spark                     = "3.3.0"
    val sttp                      = "3.5.1"
    val tapir                     = "0.20.2"
    val testcontainers            = "0.40.9"
    val testcontainersKafka       = "1.17.3"
    val zio                       = "1.0.16"
    val zioAkkaCluster            = "0.3.0"
    val zioInteropCats            = "3.2.9.1"
    val zioInteropReactiveStreams = "2.0.0-RC4"
    val zioKafka                  = "0.17.5"
    val zioLoggingSlf4j           = "2.0.0-RC6"
  }

}
