import sbt.Keys.{baseDirectory, sourceDirectory, _}
import sbt.{Def, _}
import tut.TutPlugin.autoImport.{Tut, tutSourceDirectory, tutTargetDirectory}

object TutSettings {
  
  val value: Seq[Def.Setting[_]] = Seq(
    tutSourceDirectory := (sourceDirectory in Compile).value / "docs",
    tutTargetDirectory := baseDirectory.value,
    scalacOptions in Tut --= Seq("-Xlint")
  )
  
}