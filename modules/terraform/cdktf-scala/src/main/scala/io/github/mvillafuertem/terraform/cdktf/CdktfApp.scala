package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp._
import com.hashicorp.cdktf.AppOptions

import scala.jdk.CollectionConverters._

// 1. yarn --cwd modules/terraform/cdktf-scala/ install
// 2. yarn --cwd modules/terraform/cdktf-scala/ get
// 3. sbt "project terraform-cdktf-scala;clean;run"
object CdktfApp {

  def main(args: Array[String]): Unit = {
    val app = new cdktf.App(
      AppOptions.builder()
        .stackTraces(false)
        .outdir("modules/terraform/cdktf-scala/src/main/resources/")
        .context(Map("excludeStackIdFromLogicalIds" -> true).asJava)
        .build()
    )

    new CdktfStack(app)
    app.synth()
  }

}
