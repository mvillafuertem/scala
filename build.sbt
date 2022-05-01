import org.scalajs.linker.interface.ModuleSplitStyle

import _root_.scala.sys.process.Process
import _root_.scala.{Console => csl}

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / onLoad               := {
  val GREEN = csl.GREEN
  val RESET = csl.RESET
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
    algorithms,
    `alpakka-kafka`,
    `alpakka-mongodb`,
    `aws-cdk`,
    `aws-sdk`,
    basic,
    benchmarks,
    cats,
    foundations,
    http4s,
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
  .settings(welcomeMessage)

lazy val advanced = (project in file("modules/foundations/advanced"))
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

lazy val `alpakka-kafka` = (project in file("modules/alpakka/kafka"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.`alpakka-kafka`)

lazy val `alpakka-mongodb` = (project in file("modules/alpakka/mongodb"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.`alpakka-mongodb`)

lazy val `aws-cdk` = (project in file("modules/aws/cdk"))
  .configs(IntegrationTest)
  // S E T T I N G S
  .settings(Defaults.itSettings)
  .settings(AssemblySettings.value)
  .settings(commonSettings)
  // .settings(resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/hashicorp/terraform-cdk")
  // .settings(credentials += Credentials("GitHub Package Registry", "maven.pkg.github.com", "mvillafuertem", ""))
  .settings(libraryDependencies ++= Dependencies.`aws-cdk`)

lazy val `aws-sdk` = (project in file("modules/aws/sdk"))
  .configs(IntegrationTest)
  // S E T T I N G S
  .settings(Defaults.itSettings)
  .settings(AssemblySettings.value)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.`aws-sdk`)

lazy val basic = (project in file("modules/foundations/basic"))
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
    dockerBaseImage    := "adoptopenjdk:11-hotspot",
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
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value
    )
  )
  // P L U G I N S
  .enablePlugins(GitVersioning)

lazy val algorithms = (project in file("modules/foundations/algorithms"))
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

lazy val dotty = (project in file("modules/foundations/dotty"))
  // S E T T I N G S
  .settings(scalaVersion := Settings.scala3)
  .settings(libraryDependencies ++= Dependencies.dotty)

lazy val foundations = (project in file("modules/foundations/foundations"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.foundations)

lazy val `graalvm-cli` = (project in file("modules/graalvm/cli"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.`graalvm-cli`)
  .settings(GraalVMSettings.value)
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val http4s = (project in file("modules/http4s"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.http4s)

lazy val json = (project in file("modules/json"))
  .dependsOn(algorithms)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(scalacOptions += "-Ymacro-annotations")
  .settings(libraryDependencies ++= Dependencies.json)

lazy val reflection = (project in file("modules/foundations/reflection"))
  .dependsOn(algorithms)
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.reflection)

lazy val script = (project in file("modules/script"))
// S E T T I N G S
  .settings(scalaVersion := Settings.scala213)
  .settings(crossScalaVersions := Seq(Settings.scala213))
  .settings(libraryDependencies ++= Dependencies.script)
  .settings(libraryDependencies ++= Seq("com.lihaoyi" % "ammonite" % "2.5.3" % Test cross CrossVersion.full))
  .settings(commands += Commands.ammoniteCommand)

lazy val slick = (project in file("modules/slick"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.slick)
  .settings(commands += Commands.h2Command)

lazy val slinky = (project in file("modules/slinky"))
  // S E T T I N G S
  .settings(scalaVersion := Settings.scala213)
  .settings(scalacOptions += "-Ymacro-annotations")
  .settings(Dependencies.slinky)
  .settings(scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) })
  .settings(scalaJSLinkerConfig ~= { _.withModuleSplitStyle(ModuleSplitStyle.FewestModules) })
  .settings(scalaJSLinkerConfig ~= { _.withSourceMap(false) })
  .settings(scalaJSUseMainModuleInitializer := true)
  .settings(externalNpm := {
    Process("yarn", baseDirectory.value).!
    baseDirectory.value
  })
  // S C A L A B L Y T Y P E D
  .settings(stFlavour := Flavour.Slinky)
  .settings(stMinimize := Selection.All)
  .settings(stIgnore ++= List("@material-ui/icons"))
  // P L U G I N S
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .enablePlugins(ScalaJSPlugin)

lazy val spark = (project in file("modules/spark"))
// S E T T I N G S
  .settings(scalaVersion := Settings.scala212)
  .settings(Settings.testReport ++ Information.value)
  .settings(libraryDependencies ++= Dependencies.spark)
  .settings(
    Test / fork              := false,
    Test / parallelExecution := false
  )

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

lazy val `terraform-cdktf-scalajs` = (project in file("modules/hashicorp/terraform-cdktf-scalajs"))
// S E T T I N G S
  .settings(Information.value)
  .settings(Dependencies.`terraform-cdktf-scalajs`)
  // S C A L A B L Y T Y P E D
  .settings(stMinimize := Selection.All)
  .settings(
    scalaVersion                    := Settings.scala213,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
    scalaJSUseMainModuleInitializer := true,
    /* ScalablyTypedConverterExternalNpmPlugin requires that we define how to install node dependencies and where they are */
    externalNpm                     := {
      Process("yarn", baseDirectory.value).!
      baseDirectory.value
    }
  )
  // P L U G I N S
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .enablePlugins(ScalaJSPlugin)

lazy val `terraform-cdktf-scala` = (project in file("modules/hashicorp/terraform-cdktf-scala"))
// S E T T I N G S
  .settings(Information.value)
  .settings(scalaVersion := Settings.scala213)
  .settings(libraryDependencies ++= Dependencies.`terraform-cdktf-scala`)

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

def welcomeMessage: Def.Setting[String] = onLoadMessage := {
  def header(text: String): String                = s"${csl.BOLD}${csl.MAGENTA}$text${csl.RESET}"
  def cmd(text: String, description: String = "") = f"${csl.GREEN}> ${csl.CYAN}$text%40s $description${csl.RESET}"
  // def subItem(text: String): String = s"  ${Console.YELLOW}> ${Console.CYAN}$text${Console.RESET}"

  s"""|${header("sbt")}:
      |${cmd("build", "- Prepares sources, compiles and runs tests")}
      |${cmd("testOnly [package.class] -- -z [test]", "- Runs selected tests")}
      |${cmd("prepare", "- Prepares sources by applying both scalafix and scalafmt")}
      |${cmd("fmt", "- Formats source files using scalafmt")}
      |${cmd("dependencyBrowseTree", "- It opens a browser window, but it displays a visualization of the dependency tree")}
      """.stripMargin
}
