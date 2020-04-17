Global / onLoad := {
  sLog.value.info(
    s"""*
       |*    ███████╗  ██████╗  █████╗  ██╗       █████╗
       |*    ██╔════╝ ██╔════╝ ██╔══██╗ ██║      ██╔══██╗
       |*    ███████╗ ██║      ███████║ ██║      ███████║
       |*    ╚════██║ ██║      ██╔══██║ ██║      ██╔══██║
       |*    ███████║ ╚██████╗ ██║  ██║ ███████╗ ██║  ██║
       |*    ╚══════╝  ╚═════╝ ╚═╝  ╚═╝ ╚══════╝ ╚═╝  ╚═╝
       |*    v.${version.value}
       |*""".stripMargin)
  (Global / onLoad).value
}

lazy val commonSettings = Settings.value ++ Seq(
  organization := "io.github.mvillafuertem",
  version := "0.1",
  scalaVersion := "2.13.1",
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
    `akka-untyped`,
    `akka-typed`,
    alpakka,
    algorithms,
    basic,
    cats,
    json,
    slick,
    tapir,
    todo,
    `sensor-controller`,
    `zio-akka-cluster-chat`,
    `zio-akka-cluster-sharding`
  )
  // S E T T I N G S
  .settings(commonSettings)
  .settings(Settings.noPublish)

lazy val advanced = (project in file("modules/advanced"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.advanced)

lazy val `akka-untyped` = (project in file("modules/akka-untyped"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(NexusSettings.value)
  .settings(libraryDependencies ++= Dependencies.`akka-untyped`)

lazy val `akka-typed` = (project in file("modules/akka-typed"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(NexusSettings.value)
  .settings(libraryDependencies ++= Dependencies.`akka-typed`)

lazy val alpakka = (project in file("modules/alpakka"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.alpakka)

lazy val basic = (project in file("modules/basic"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.basic)

lazy val todo = (project in file("modules/applications/todo"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(BuildInfoSettings.value)
  .settings(libraryDependencies ++= Dependencies.todo)
  // P L U G I N S
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)

lazy val `sensor-controller` = (project in file("modules/applications/sensor-controller"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.`sensor-controller`)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ))
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
  .settings(libraryDependencies ++= Dependencies.json)

lazy val slick = (project in file("modules/slick"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.slick)

lazy val tapir = (project in file("modules/tapir"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(BuildInfoSettings.value)
  .settings(libraryDependencies ++= Dependencies.tapir)
  // P L U G I N S
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)

lazy val `zio-akka-cluster-chat` = (project in file("modules/zio/akka-cluster-chat"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.zio)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))

lazy val `zio-akka-cluster-sharding` = (project in file("modules/zio/akka-cluster-sharding"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.zio)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))