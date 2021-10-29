import com.typesafe.sbt.GitPlugin.autoImport.git
import sbt.Keys._
import sbt.{ Def, SettingKey }
import sbtbuildinfo.BuildInfoKeys.buildInfoKeys
import sbtbuildinfo.BuildInfoPlugin.autoImport.{ buildInfoOptions, buildInfoPackage, BuildInfoKey, BuildInfoOption }

object BuildInfoSettings {

  private val gitCommitString = SettingKey[String]("gitCommit")

  val value: Seq[Def.Setting[_]] = Seq(
    buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, gitCommitString),
    buildInfoPackage := s"${organization.value}.${name.value}",
    buildInfoOptions ++= Seq(BuildInfoOption.ToJson, BuildInfoOption.BuildTime),
    gitCommitString  := git.gitHeadCommit.value.getOrElse("Not Set")
  )

}
