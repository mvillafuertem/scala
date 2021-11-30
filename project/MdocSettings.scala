import mdoc.MdocPlugin.autoImport._
import sbt.Keys.{ baseDirectory, sbtVersion, scalaVersion, version }
import sbt._

object MdocSettings {

  val value: Seq[Def.Setting[_]] = Seq(
    mdocIn        := baseDirectory.value / "src/main/mdoc/",
    mdocOut       := file("."),
    mdocVariables := Map(
      "PROJECT_NAME"  -> "Scala",
      "VERSION"       -> version.value,
      "SCALA_VERSION" -> scalaVersion.value,
      "SBT_VERSION"   -> sbtVersion.value
    )
  )

}
