package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp._
import com.hashicorp.cdktf.{ AppOptions, S3Backend }

import scala.jdk.CollectionConverters._

// 1. yarn --cwd modules/hashicorp/erraform-cdktf-scala/ install
// 2. yarn --cwd modules/hashicorp/erraform-cdktf-scala/ get
// 3. sbt "project terraform-cdktf-scala;clean;run"
// 4. yarn --cwd modules/hashicorp/terraform-cdktf-scala/ plan
// 5. yarn --cwd modules/hashicorp/terraform-cdktf-scala/ apply
object CdktfApp {

  def main(args: Array[String]): Unit = {

    val app = new cdktf.App(
      AppOptions
        .builder()
        .stackTraces(false)
        .outdir("modules/hashicorp/terraform-cdktf-scala/src/main/resources/")
        .context(Map("excludeStackIdFromLogicalIds" -> true).asJava)
        .build()
    )

    val stack: CdktfStack = new CdktfStack(app)

    val _: S3Backend = S3Backend.Builder
      .create(stack)
      .bucket("cdktf")
      .key("terraform.tfstate")
      .region("eu-west-1")
      .build()

    app.synth()

  }

}
