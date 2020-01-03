import sbt.Keys.{exportJars, _}
import sbt.{Def, Tests, _}

object Settings {

  val value: Seq[Def.Setting[_]] = Seq(
    
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
      if (version.value.endsWith("SNAPSHOT")) {
        default :+ "-Xcheckinit"
      } else {
        default
      } // check against early initialization
    },
    
    javaOptions += "-Duser.timezone=UTC",
    
    Test / fork := false,

    Test / parallelExecution := false,

    Test / testOptions ++= Seq(
      Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
      Tests.Argument("-oDF")
    ),

    Global / cancelable := true,
    // OneJar
    exportJars := true
  )

  val noPublish: Seq[Def.Setting[_]] = Seq(
    publish / skip := true
  )

  ThisBuild / useCoursier := false

//  val noAssemblyTest: Seq[Def.Setting[_]] = Seq(
//    assembly / test := {}
//  )
  
}
