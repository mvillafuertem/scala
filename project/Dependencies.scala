import sbt._

object Dependencies {

  val production: Seq[ModuleID] = Seq(
    // Akka
    "com.typesafe.akka" %% "akka-actor" % Versions.akka,
    "com.typesafe.akka" %% "akka-stream" % Versions.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Versions.akka,
    "com.typesafe.akka" %% "akka-stream-kafka" % Versions.akka_kafka,
    "com.lightbend.akka" %% "akka-stream-alpakka-slick" % Versions.akka_slick
  )
  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % Versions.scala_test % Test,
    "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
    "com.typesafe.akka" %% "akka-stream-kafka-testkit" % Versions.akka_kafka_test % Test
  )

  object Versions {
    val akka = "2.5.21"
    val akka_kafka = "1.0-RC1"
    val akka_slick = "1.0-M3"
    val scala_test = "3.0.5"
    val akka_kafka_test = "1.0.1"
  }

}
