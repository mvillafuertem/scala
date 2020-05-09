Global / onLoad := {
  println(s"""
             |\u001b[32m    ███████╗  ██████╗  █████╗  ██╗       █████╗
             |\u001b[32m    ██╔════╝ ██╔════╝ ██╔══██╗ ██║      ██╔══██╗
             |\u001b[32m    ███████╗ ██║      ███████║ ██║      ███████║
             |\u001b[32m    ╚════██║ ██║      ██╔══██║ ██║      ██╔══██║
             |\u001b[32m    ███████║ ╚██████╗ ██║  ██║ ███████╗ ██║  ██║
             |\u001b[32m    ╚══════╝  ╚═════╝ ╚═╝  ╚═╝ ╚══════╝ ╚═╝  ╚═╝
             |    v.${version.value}
             |""".stripMargin)
  sLog.value.info(s"""
                     |\u001b[32m    ███████╗  ██████╗  █████╗  ██╗       █████╗
                     |\u001b[32m    ██╔════╝ ██╔════╝ ██╔══██╗ ██║      ██╔══██╗
                     |\u001b[32m    ███████╗ ██║      ███████║ ██║      ███████║
                     |\u001b[32m    ╚════██║ ██║      ██╔══██║ ██║      ██╔══██║
                     |\u001b[32m    ███████║ ╚██████╗ ██║  ██║ ███████╗ ██║  ██║
                     |\u001b[32m    ╚══════╝  ╚═════╝ ╚═╝  ╚═╝ ╚══════╝ ╚═╝  ╚═╝
                     |    v.${version.value}
                     |""".stripMargin)
  (Global / onLoad).value
}

lazy val commonSettings = Settings.value ++ Seq(
  organization := "io.github.mvillafuertem",
  version := "0.1",
  scalaVersion := "2.13.2",
  homepage := Some(url("https://github.com/mvillafuertem/scala")),
  licenses := List("MIT" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "mvillafuertem",
      "Miguel Villafuerte",
      "mvillafuertem@email.com",
      url("https://github.com/mvillafuertem")
    )
  )
)

lazy val scala = (project in file("."))
  .aggregate(
    advanced,
    `akka-classic`,
    `akka-typed`,
    `akka-fsm`,
    `akka-http`,
    `sensor-controller`,
    alpakka,
    algorithms,
    basic,
    cats,
    json,
    slick,
    sttp,
    tapir,
    `zio-akka-cluster-chat`,
    `zio-akka-cluster-sharding`,
    `zio-queues`
  )
  // S E T T I N G S
  .settings(commonSettings)
  .settings(Settings.noPublish)
  .settings(commands ++= Commands.value)

lazy val advanced = (project in file("modules/advanced"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.advanced)

lazy val `akka-classic` = (project in file("modules/akka/classic"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(NexusSettings.value)
  .settings(libraryDependencies ++= Dependencies.`akka-classic`)

lazy val `akka-typed` = (project in file("modules/akka/typed"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(NexusSettings.value)
  .settings(libraryDependencies ++= Dependencies.`akka-typed`)

lazy val alpakka = (project in file("modules/akka/alpakka"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.alpakka)

lazy val basic = (project in file("modules/basic"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.basic)

lazy val `akka-fsm` = (project in file("modules/akka/fsm"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(BuildInfoSettings.value)
  .settings(buildInfoPackage := s"${organization.value}.akka.fsm")
  .settings(libraryDependencies ++= Dependencies.`akka-fsm`)
  // P L U G I N S
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)

lazy val `akka-http` = (project in file("modules/akka/http"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(BuildInfoSettings.value)
  .settings(buildInfoPackage := s"${organization.value}.akka.http")
  .settings(libraryDependencies ++= Dependencies.`akka-http`)
  // P L U G I N S
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)

lazy val `sensor-controller` = (project in file("modules/akka/sensor-controller"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.`sensor-controller`)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )
  // P L U G I N S
  .enablePlugins(GitVersioning)

lazy val algorithms = (project in file("modules/algorithms"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.algorithms)

lazy val cats = (project in file("modules/cats"))
  .dependsOn(algorithms)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.cats)

lazy val json = (project in file("modules/json"))
  .dependsOn(algorithms)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(scalacOptions += "-Ymacro-annotations")
  .settings(libraryDependencies ++= Dependencies.json)

lazy val slick = (project in file("modules/slick"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.slick)

lazy val sttp = (project in file("modules/sttp"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(scalacOptions += "-Ymacro-annotations")
  .settings(libraryDependencies ++= Dependencies.sttp)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))

lazy val tapir = (project in file("modules/tapir"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(BuildInfoSettings.value)
  .settings(libraryDependencies ++= Dependencies.tapir)
  // P L U G I N S
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)

lazy val zio: Project => Project =
  // S E T T I N G S
  _.settings(commonSettings)
    .settings(libraryDependencies ++= Dependencies.zio)
    .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))

lazy val `zio-akka-cluster-chat` = (project in file("modules/zio/akka-cluster-chat"))
  .configure(zio)

lazy val `zio-akka-cluster-sharding` = (project in file("modules/zio/akka-cluster-sharding"))
  .configure(zio)

lazy val `zio-queues` = (project in file("modules/zio/queues"))
  .configure(zio)

lazy val `zio-streams` = (project in file("modules/zio/streams"))
  .configure(zio)
