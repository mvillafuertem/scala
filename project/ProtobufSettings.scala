import sbt.Keys.target
import sbt.{ Compile, _ }
import sbtprotoc.ProtocPlugin.autoImport.PB

object ProtobufSettings {

  lazy val value: Seq[Def.Setting[_]] = Seq(
    // PB.externalIncludePath := target.value / "protobuf_external",
    Compile / PB.protoSources ++= Seq(
      target.value / "protobuf_external"
    ),
    Compile / PB.protocOptions := Seq(
      "--descriptor_set_out=" + target.value / "descriptor.proto"
    )
    // https://stackoverflow.com/questions/52628371/generate-file-descriptor-set-desc-with-scalapb
    // Compile / PB.targets += (PB.gens.descriptorSet -> (Compile / crossTarget).value / "descriptor.protoset")
  )

}
