package io.github.mvillafuertem.terraform.cdktf

import software.constructs.Construct

import com.hashicorp.cdktf.App
import com.hashicorp.cdktf.TerraformStack
import com.hashicorp.cdktf.AppOptions
import software.constructs.Construct

import scala.jdk.CollectionConverters._

// 1. yarn --cwd modules/terraform/cdktf-scala/ install
// 2. yarn --cwd modules/terraform/cdktf-scala/ get
object CdktfApp:

  class MyStack(scope: Construct, id: String) extends TerraformStack(scope, id)


  def main(args: Array[String]): Unit =
    val context = Map(
      ("excludeStackIdFromLogicalIds" -> true),
      ("excludeStackIdFromLogicalIds" -> true)
    ).asJava

    val app = new App(
      AppOptions.Builder()
        .stackTraces(false)
        .outdir("modules/terraform/cdktf-scalajs/src/main/resources/")
        .context(context)
        .build()
    )
    new MyStack(app, "Hello")
    app.synth()

