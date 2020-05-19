Global / onLoad := {
  val GREEN = "\u001b[32m"
  val RESET = "\u001b[0m"
  println(s"""$GREEN
             |$GREEN        ███████╗  ██████╗  █████╗  ██╗       █████╗
             |$GREEN        ██╔════╝ ██╔════╝ ██╔══██╗ ██║      ██╔══██╗
             |$GREEN        ███████╗ ██║      ███████║ ██║      ███████║
             |$GREEN        ╚════██║ ██║      ██╔══██║ ██║      ██╔══██║
             |$GREEN        ███████║ ╚██████╗ ██║  ██║ ███████╗ ██║  ██║
             |$GREEN        ╚══════╝  ╚═════╝ ╚═╝  ╚═╝ ╚══════╝ ╚═╝  ╚═╝
             |$RESET        v.${version.value}
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
    reflection,
    slick,
    sttp,
    tapir,
    `zio-akka-cluster-chat`,
    `zio-akka-cluster-sharding`,
    `zio-kafka`,
    `zio-queues`,
    `zio-streams`,
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

lazy val reflection = (project in file("modules/reflection"))
  .dependsOn(algorithms)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.reflection)

lazy val slick = (project in file("modules/slick"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.slick)

lazy val slinky = (project in file("modules/slinky"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(scalacOptions += "-Ymacro-annotations")
  .settings(
    Compile / npmDependencies ++= Seq(
      "react"            -> "16.13.1",
      "react-dom"        -> "16.13.1",
      "@types/react"     -> "16.9.34",
      "@types/react-dom" -> "16.9.6"
    )
  )
  .settings(Compile / npmDevDependencies += "copy-webpack-plugin" -> "5.1.1")
  .settings(Compile / npmDevDependencies += "css-loader" -> "3.4.2")
  .settings(Compile / npmDevDependencies += "file-loader" -> "5.1.0")
  .settings(Compile / npmDevDependencies += "html-webpack-plugin" -> "3.2.0")
  .settings(Compile / npmDevDependencies += "style-loader" -> "1.1.3")
  .settings(Compile / npmDevDependencies += "webpack-merge" -> "4.2.2")
  .settings(Compile / npmDevDependencies += "relaxedjs" -> "0.2.4")
  .enablePlugins(ScalablyTypedConverterPlugin)
  .settings(
    useYarn := true,
    webpackDevServerPort := 8080,
    stFlavour := Flavour.Slinky,
    stEnableScalaJsDefined := Selection.AllExcept("@material-ui/core"),
    stIgnore ++= List("@material-ui/icons"),
    Compile / npmDependencies ++= Seq(
      "@material-ui/core" -> "3.9.3" // note: version 4 is not supported yet
    )
  )
  .settings(scalaJSUseMainModuleInitializer := true)
  .settings(fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly())
  .settings(fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"))
  .settings(fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"))
  .settings(fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"))
  .settings(startWebpackDevServer / version := "3.10.3")
  .settings(Test / requireJsDomEnv := true)
  .settings(Test / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"))
  .settings(webpack / version := "4.41.6")
  .settings(webpackResources := baseDirectory.value / "webpack" * "*")
  .settings(
    Compile / fastOptJS / webpackExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackExtraArgs += "--mode=production",
    Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production"
  )
  .settings(Dependencies.slinky)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
  // P L U G I N S
  .enablePlugins(ScalaJSBundlerPlugin)

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

lazy val `zio-kafka` = (project in file("modules/zio/kafka"))
  .configure(zio)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)

lazy val `zio-queues` = (project in file("modules/zio/queues"))
  .configure(zio)

lazy val `zio-streams` = (project in file("modules/zio/streams"))
  .configure(zio)
