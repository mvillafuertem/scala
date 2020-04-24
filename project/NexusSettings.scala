import sbt.Keys.{ isSnapshot, publishTo, resolvers }
import sbt._

object NexusSettings {

  val host    = ""
  val baseUrl = ""

  val value: Seq[Def.Setting[_]] = Seq(
    resolvers += Resolver.bintrayRepo("dnvriend", "maven"),
    publishTo := {

      if (isSnapshot.value)
        Some("snapshots" at baseUrl + "/repository/libs-snapshot-local")
      else
        Some("releases" at baseUrl + "/repository/libs-release-local")
    }
  )

}
