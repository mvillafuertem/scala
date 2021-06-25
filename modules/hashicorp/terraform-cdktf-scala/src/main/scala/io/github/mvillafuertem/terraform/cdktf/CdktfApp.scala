package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp._
import com.hashicorp.cdktf.{AppOptions, S3Backend}
import io.github.mvillafuertem.terraform.cdktf.CdktfApp.app

import scala.jdk.CollectionConverters._

// 1. yarn --cwd modules/hashicorp/terraform-cdktf-scala/ install
// 2. yarn --cwd modules/hashicorp/terraform-cdktf-scala/ get
// 3. sbt "project terraform-cdktf-scala;run"
object CdktfApp {
  val app = new cdktf.App(
    AppOptions
      .builder()
      .stackTraces(false)
      .outdir("modules/hashicorp/terraform-cdktf-scala/src/main/resources/")
      .context(Map("excludeStackIdFromLogicalIds" -> true).asJava)
      .build()
  )
}

// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ plan
// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ apply
object CdktfStackApp extends App {
  val stack: CdktfStack = new CdktfStack(app)
  val _: S3Backend      = S3Backend.Builder
    .create(stack)
    .bucket("cdktf")
    .key("terraform.tfstate")
    .region("eu-west-1")
    .dynamodbTable("cdktf-dynamodb")
    .encrypt(true)
    .build()

  app.synth()
}

// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ planState
// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ applyState
// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ destroyState
object CdktfStateApp extends App {
  val _ = new CdktfState(app)
  app.synth()
}
