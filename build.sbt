import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

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
    `zio-streams`
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

lazy val aws = (project in file("modules/aws"))
// S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.aws)

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

lazy val docs = (project in file("modules/docs"))
  .configure(browserProject)
  // S E T T I N G S
  .settings(commonSettingsJs)
  .settings(webpackDevServerPort := 8008)
  .settings(Test / requireJsDomEnv := true)
  .settings(
    Compile / npmDependencies ++= Seq(
      "react"                    -> "16.13.1",
      "react-dom"                -> "16.13.1",
      "react-router-dom"         -> "5.2.0",
      "react-proxy"              -> "1.1.8",
      "remark"                   -> "8.0.0",
      "remark-react"             -> "4.0.1",
      "react-helmet"             -> "5.2.0",
      "react-syntax-highlighter" -> "6.0.4"
    )
  )
  .settings(
    Compile / fastOptJS / webpackExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackExtraArgs += "--mode=production",
    Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production"
  )
  .settings(Dependencies.docs)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
  .settings(MdocSettings.value)
  // P L U G I N S
  .enablePlugins(ScalaJSBundlerPlugin)
  //.enablePlugins(ScalablyTypedConverterPlugin)
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(MdocPlugin)

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

/**
 * Implement the `start` and `dist` tasks defined above.
 * Most of this is really just to copy the index.html file around.
 */
lazy val browserProject: Project => Project =
  _.settings(
    dist := {
      val artifacts      = (Compile / fullOptJS / webpack).value
      val artifactFolder = (Compile / fullOptJS / crossTarget).value
      val distFolder     = (ThisBuild / baseDirectory).value / "docs"

      distFolder.mkdirs()
      artifacts.foreach { artifact =>
        val target = artifact.data.relativeTo(artifactFolder) match {
          case None          => distFolder / artifact.data.name
          case Some(relFile) => distFolder / relFile.toString
        }

        Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
      }

      val indexFrom = baseDirectory.value / "src/main/js/index.html"
      val indexTo   = distFolder / "index.html"

      val indexPatchedContent = {
        import collection.JavaConverters._
        Files
          .readAllLines(indexFrom.toPath, IO.utf8)
          .asScala
          .map(_.replaceAllLiterally("-fastopt-", "-opt-"))
          .mkString("\n")
      }

      Files.write(indexTo.toPath, indexPatchedContent.getBytes(IO.utf8))
      distFolder
    }
  )

lazy val slinky = (project in file("modules/slinky"))
  // S E T T I N G S
  .settings(commonSettingsJs)
  .settings(webpackDevServerPort := 8008)
  .settings(startWebpackDevServer / version := "3.10.3")
  .settings(Test / requireJsDomEnv := true)
  .settings(stFlavour := Flavour.Slinky)
  .settings(stIgnore ++= List("@material-ui/icons"))
  .settings(
    Compile / npmDependencies ++= Seq(
      "react"               -> "16.13.1",
      "react-dom"           -> "16.13.1",
      "@types/react"        -> "16.9.34",
      "@types/react-dom"    -> "16.9.6",
      "@material-ui/core"   -> "3.9.3", // note: version 4 is not supported yet
      "@material-ui/styles" -> "3.0.0-alpha.10" // note: version 4 is not supported yet
    )
  )
  .settings(
    Compile / fastOptJS / webpackExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackExtraArgs += "--mode=production",
    Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production"
  )
  .settings(Dependencies.slinky)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
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
