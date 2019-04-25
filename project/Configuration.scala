import sbt.Keys.{exportJars, _}
import sbt.{Def, Tests, _}

object Configuration {
  
  val commonSettings: Seq[Def.Setting[_ >: String with Task[Seq[String]] with Boolean with Task[Seq[TestOption]]]] = Seq(
    
    scalacOptions := {
      val default = Seq(
        "-deprecation",
        "-feature",
        "-language:higherKinds",
        "-unchecked",
        "-Xfatal-warnings",
        "-Xlint",
        "-Ypartial-unification"
      )
      if (version.value.endsWith("SNAPSHOT")) {
        default :+ "-Xcheckinit"
      } else {
        default
      } // check against early initialization
    },
    
    scalacOptions in(Test, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"),
    scalacOptions in(Test, console) ++= Seq("-Ywarn-unused:-imports"),
    
    javaOptions += "-Duser.timezone=UTC",
    
    fork in Test := false,
    
    parallelExecution in Test := false,
    
    testOptions in Test ++= Seq(
      Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
      Tests.Argument("-oDF")
    ),
    
    cancelable in Global := true,
    // OneJar
    exportJars := true
  )
  
}
