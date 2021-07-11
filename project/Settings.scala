import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys.{ exportJars, _ }
import sbt.{ Def, Tests, _ }
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.{ useYarn, webpackDevServerExtraArgs, webpackExtraArgs }

object Settings {

  lazy val valueJs: Seq[Def.Setting[_]] = value ++ Seq(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= (/* disabled because it somehow triggers many warnings */
    _.withSourceMap(false)
      .withModuleKind(ModuleKind.CommonJSModule)),
    scalacOptions += "-Ymacro-annotations",
    // S C A L A J S  B U N D L E R
    useYarn := true,
    // W E B P A C K
    Compile / fastOptJS / webpackExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackExtraArgs += "--mode=production",
    Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production"
    // Execute the tests in browser-like environment
    // Test / requireJsDomEnv   := true,
    // webpackResources := baseDirectory.value / "webpack" * "*"
    // Test / jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
    // Test / jsSourceDirectories += baseDirectory.value / "resources"
    // Test / unmanagedResourceDirectories += baseDirectory.value / "node_modules"
  )

  lazy val scala213 = "2.13.6"
  lazy val scala212 = "2.12.14"
  lazy val scala3   = "3.0.1"

  lazy val value: Seq[Def.Setting[_]] = Seq(
    // autoScalaLibrary := false,
    scalaVersion := scala213,
    scalacOptions := {
      val default = Seq(
        "-deprecation",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-language:reflectiveCalls",
        "-unchecked",
        //"-Xfatal-warnings",
        "-Xlint"
      )
      if (version.value.endsWith("SNAPSHOT"))
        default :+ "-Xcheckinit"
      else
        default
      // check against early initialization
    },
    javaOptions += "-Duser.timezone=UTC",
    Test / fork := false,
    Test / parallelExecution := false,
    IntegrationTest / fork := false,
    IntegrationTest / parallelExecution := false,
    Global / cancelable := true,
    // OneJar
    exportJars := true
  )

  lazy val testReport: Seq[Def.Setting[_]] = Seq(
    Test / testOptions ++= Seq(
      Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
      Tests.Argument("-oDF")
    )
  )

  lazy val noPublish: Seq[Def.Setting[_]] = Seq(
    publish / skip := true
  )

  ThisBuild / useCoursier := false

}
