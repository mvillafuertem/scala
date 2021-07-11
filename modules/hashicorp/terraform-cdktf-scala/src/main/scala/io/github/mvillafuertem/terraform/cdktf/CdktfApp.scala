package io.github.mvillafuertem.terraform.cdktf

import com.hashicorp._
import com.hashicorp.cdktf.{ AppOptions, S3Backend }
import io.github.mvillafuertem.terraform.cdktf.CdktfStack.CdktfStackConfiguration

import scala.io.Source
import scala.jdk.CollectionConverters._

// 1. yarn --cwd modules/hashicorp/terraform-cdktf-scala/ install
// 2. yarn --cwd modules/hashicorp/terraform-cdktf-scala/ get

// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ plan
// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ apply
// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ destroy

// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ planState
// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ applyState
// yarn --cwd modules/hashicorp/terraform-cdktf-scala/ destroyState
object CdktfApp extends App {

  private val app: cdktf.App = new cdktf.App(
    AppOptions
      .builder()
      .stackTraces(false)
      .outdir("modules/hashicorp/terraform-cdktf-scala/src/main/resources/")
      .context(
        Map(
          "excludeStackIdFromLogicalIds" -> true,
          "allowSepCharsInLogicalIds"    -> true
        ).asJava
      )
      .build()
  )

  private def getContentFile(name: String): String =
    Source
      .fromInputStream(getClass.getResourceAsStream(name))
      .getLines()
      .mkString("\n")

  private val publicKey: String = getContentFile("/ssh/id_rsa.pub")
  private val userData: String  = getContentFile("/userData.yml")

  private val devConfiguration: CdktfStackConfiguration =
    CdktfStackConfiguration(
      "accessKey",
      "000000000000",
      "cdktf-dev",
      "cdktf-dynamodb",
      "dev",
      "saml",
      publicKey,
      "eu-west-1",
      "secretKey",
      "~/.aws/credentials",
      userData
    )

  private val _: CdktfState        = new CdktfState(app, "cdktf-terraform-state", devConfiguration)
  private val devStack: CdktfStack = new CdktfStack(app, "cdktf-terraform-stack", devConfiguration)
  private val _: S3Backend         = S3Backend.Builder
    .create(devStack)
    .bucket(devConfiguration.bucket)
    .key("terraform.tfstate")
    .region(devConfiguration.region)
    .dynamodbTable(devConfiguration.dynamodbTable)
    .sharedCredentialsFile(devConfiguration.sharedCredentialsFile)
    .profile(devConfiguration.profile)
    .encrypt(true)
    .build()

  app.synth()
}
