import sbt.Keys.sLog
import sbt.{ taskKey, Compile, Def }
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.npmInstallDependencies

import scala.sys.process._

object Tasks {

  private val synth   = taskKey[Int]("cdktf synth")
  private val deploy  = taskKey[Int]("cdktf deploy")
  private val destroy = taskKey[Int]("cdktf destroy")
  private val plan    = taskKey[Int]("terraform plan")
  private val apply   = taskKey[Int]("terraform apply")

  val cdktfTask: Seq[Def.Setting[_]] = Seq(
    synth   := {
      val log     = sLog.value
      val result  = (Compile / npmInstallDependencies).value
      log.success("---------------------------------------------")
      log.success("R U N N I N G  S Y N T H  C O M M A N D")
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
    },
    deploy  := {
      val log     = sLog.value
      val result  = (Compile / npmInstallDependencies).value
      log.success("---------------------------------------------")
      log.success("R U N N I N G  D E P L O Y  C O M M A N D")
      val command = Seq(
        s"${result.getPath}/node_modules/cdktf-cli/bin/cdktf",
        "deploy",
        "--app",
        "sbt terraform-cdktf/run",
        "--log-level=DEBUG",
        "--output=modules/terraform-cdktf/src/main/resources",
        "--json"
      )
      Process(command).!
    },
    destroy := {
      val log     = sLog.value
      val result  = (Compile / npmInstallDependencies).value
      log.success("---------------------------------------------")
      log.success("R U N N I N G  D E S T R O Y  C O M M A N D")
      val command = Seq(
        s"${result.getPath}/node_modules/cdktf-cli/bin/cdktf",
        "destroy",
        "--app",
        "sbt terraform-cdktf/run",
        "--log-level=DEBUG",
        "--output=modules/terraform-cdktf/src/main/resources",
        "--json"
      )
      Process(command).!
    },
    plan    := {
      val log     = sLog.value
      val result  = (Compile / npmInstallDependencies).value
      log.success("---------------------------------------------")
      log.success("R U N N I N G  P L A N  C O M M A N D")
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
  )

}
