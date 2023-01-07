import sbt.Keys.target
import sbt._
import sbtprotoc.ProtocPlugin.autoImport.PB

object ProtobufSettings {

  lazy val value: Seq[Def.Setting[_]] = Seq(
    // PB.externalIncludePath := target.value / "protobuf_external",
    Compile / PB.protoSources ++= Seq(
      target.value / "protobuf_external"
    ),
    Compile / PB.protocOptions := Seq(
      "--descriptor_set_out=" + target.value / "descriptor"
    )
  )

}
