import sbt.Keys.sLog
import sbt.{ taskKey, Compile, Def }
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.npmInstallDependencies

import scala.sys.process._

object Tasks {

  private val cdktf = taskKey[Int]("cdktf synth")

  val cdktfTask: Def.Setting[_] = {
    cdktf := {
      val log     = sLog.value
      val result  = (Compile / npmInstallDependencies).value
      log.success("---------------------------------------------")
      log.success("R U N I N G  S Y N T H  C O M M A N D")
      val command = Seq(
        s"${result.getPath}/node_modules/cdktf-cli/bin/cdktf",
        "synth",
        "--app",
        "sbt terraform-cdktf/run",
        "--log-level=DEBUG",
        "--output=modules/terraform-cdktf/src/main/resources",
        "--json"
      )
      Process(command).!
    }
  }

}
