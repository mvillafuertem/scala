import scalajsbundler.util.JSON._

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

lazy val commonSettingsJs = Settings.valueJs ++ Information.value

lazy val commonSettings = Settings.value ++ Settings.testReport ++ Information.value

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
    `aws-cdk`,
    `aws-sdk`,
    basic,
    benchmarks,
    cats,
    json,
    reflection,
    script,
    slick,
    // slinky, scalajs Error downloading org.scoverage
    spark,
    sttp,
    tapir,
    // `terraform-cdktf`, scalajs Error downloading org.scoverage
    `zio-akka-cluster-chat`,
    `zio-akka-cluster-sharding`,
    `zio-kafka`,
    `zio-queues`,
    `zio-schedule`,
    `zio-streams`
  )
  // S E T T I N G S
  .settings(commonSettings)
  .settings(Settings.noPublish)
  .settings(commands ++= Commands.value)
  .settings(commands += Commands.frontendDevCommand("slinky"))
  .settings(commands += Commands.frontendDevCommand("docs"))

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

lazy val `aws-cdk` = (project in file("modules/aws/cdk"))
  .configs(IntegrationTest)
  // S E T T I N G S
  .settings(Defaults.itSettings)
  .settings(AssemblySettings.value)
  .settings(commonSettings)
  //.settings(resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/hashicorp/terraform-cdk")
  //.settings(credentials += Credentials("GitHub Package Registry", "maven.pkg.github.com", "mvillafuertem", ""))
  .settings(libraryDependencies ++= Dependencies.`aws-cdk`)

lazy val `aws-sdk` = (project in file("modules/aws/sdk"))
  .configs(IntegrationTest)
  // S E T T I N G S
  .settings(Defaults.itSettings)
  .settings(AssemblySettings.value)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.`aws-sdk`)

lazy val basic = (project in file("modules/basic"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.basic)

lazy val benchmarks = (project in file("modules/benchmarks"))
// S E T T I N G S
  .settings(commonSettings)
  .enablePlugins(JmhPlugin)

lazy val cask = (project in file("modules/cask"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.cask)
  .settings(
    dockerBaseImage := "adoptopenjdk:11-hotspot",
    dockerExposedPorts := Seq(8080)
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

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

lazy val docs = (project in file("modules/docs"))
  .configure(WebpackSettings.browserProject)
  // S E T T I N G S
  .settings(commonSettingsJs)
  .settings(webpackDevServerPort := 8008)
  .settings(Test / requireJsDomEnv := true)
  .settings(Compile / npmDependencies ++= NpmDependencies.docs)
  .settings(Dependencies.docs)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
  .settings(MdocSettings.value)
  // P L U G I N S
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(MdocPlugin)

lazy val dotty = (project in file("modules/dotty"))
// S E T T I N G S
  .settings(scalaVersion := "0.27.0-RC1")
  .settings(libraryDependencies ++= Dependencies.dotty.map(_.withDottyCompat(scalaVersion.value)))
  .enablePlugins(DottyPlugin)
// P L U G I N S

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

lazy val script = (project in file("modules/script"))
// S E T T I N G S
  .settings(scalaVersion := "2.13.3")
  .settings(libraryDependencies ++= Dependencies.script)
  .settings(libraryDependencies ++= Seq("com.lihaoyi" % "ammonite" % "2.2.0" % Test cross CrossVersion.full))
  .settings(commands += Commands.ammoniteCommand)

lazy val slick = (project in file("modules/slick"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.slick)
  .settings(commands += Commands.h2Command)

lazy val slinky = (project in file("modules/slinky"))
  // S E T T I N G S
  .settings(commonSettingsJs)
  .settings(Dependencies.slinky)
  // S C A L A  J S  B U N D L E R
  .settings(webpackDevServerPort := 8008)
  .settings(startWebpackDevServer / version := "3.10.3")
  .settings(Test / requireJsDomEnv := true)
  .settings(Compile / npmDependencies ++= NpmDependencies.slinky)
  // S C A L A B L Y T Y P E D
  .settings(stFlavour := Flavour.Slinky)
  .settings(stMinimize := Selection.All)
  .settings(stIgnore ++= List("@material-ui/icons"))
  // P L U G I N S
  .enablePlugins(ScalablyTypedConverterPlugin)
  .enablePlugins(ScalaJSPlugin)

lazy val spark = (project in file("modules/spark"))
// S E T T I N G S
  .settings(scalaVersion := "2.12.12")
  .settings(Settings.testReport ++ Information.value)
  .settings(libraryDependencies ++= Dependencies.spark)

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
  .settings(commands += Commands.h2Command)
  // P L U G I N S
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(GitVersioning)

lazy val `terraform-cdktf` = (project in file("modules/terraform-cdktf"))
// S E T T I N G S
  .settings(commonSettingsJs)
  .settings(Dependencies.`terraform-cdktf`)
  // S C A L A  J S  B U N D L E R
  .settings(Compile / npmDependencies ++= NpmDependencies.`terraform-cdktf`)
  .settings(Compile / npmDevDependencies ++= NpmDependencies.`dev-terraform-cdktf`)
  .settings(
    additionalNpmConfig in Compile := Map(
      "name"    -> str("scalajs-cdktf"),
      "version" -> str(version.value),
      "license" -> str("MIT")
    )
  )
  .settings(
    Test / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"),
    // Execute the tests in browser-like environment
    // Test / requireJsDomEnv   := true,
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"),
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"),
    fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"),
    webpackResources := baseDirectory.value / "webpack" * "*"
    // Test / jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
    // Test / jsSourceDirectories += baseDirectory.value / "resources"
    // Test / unmanagedResourceDirectories += baseDirectory.value / "node_modules"
  )
  // S C A L A B L Y T Y P E D
  .settings(stIgnore ++= List("cdktf-cli"))
  .settings(stMinimize := Selection.All)
  .settings(Tasks.cdktfTask)
  // P L U G I N S
  .enablePlugins(ScalablyTypedConverterPlugin)
  .enablePlugins(ScalaJSPlugin)

lazy val cdktf = taskKey[Unit]("cdktf synth")

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

lazy val `zio-schedule` = (project in file("modules/zio/schedule"))
  .configure(zio)

lazy val `zio-streams` = (project in file("modules/zio/streams"))
  .configure(zio)
