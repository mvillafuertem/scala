package io.github.mvillafuertem.terraform.cdktf

import typings.cdktf.appMod.AppOptions
import typings.cdktf.mod.{ App, S3Backend }
import typings.cdktf.s3BackendMod.S3BackendProps
import typings.cdktf.terraformModuleMod.TerraformModuleOptions
import typings.cdktfProviderAws.awsProviderMod.AwsProviderConfig
import typings.cdktfProviderAws.mod.AwsProvider
// 1. cdktf get
// 2. sbt terraform-cdktf/run
// 3. mv `pwd`/modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/main `pwd`/modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf
// 4. tsc --declaration --project modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf
// 6. cp -R `pwd`/modules/terraform/cdktf/.gen/ `pwd`/modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf/node_modules/scalajs-cdktf
// 7. sbt "project stc;stc -d modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf --includeProject=true"
// 8. cdktf synth -a "sbt terraform-cdktf/run" --log-level=DEBUG --json --output modules/terraform-cdktf/src/test/resources

// aws s3api create-bucket --bucket "${BUCKET_NAME}" --region "${BUCKET_REGION}" --create-bucket-configuration LocationConstraint="${BUCKET_REGION}"
/*

sbt terraform-cdktf/synth && \
cd /Users/mvillafuerte/Projects/scala/modules/terraform-cdktf/src/main/resources/ && \
terraform apply && \
cd -
AWS account ID:

 */
object CdktfApp {

  def main(args: Array[String]): Unit = {
    val app = new App(AppOptions().setStackTraces(false).setOutdir("modules/terraform-cdktf/src/main/resources/"))
    val _   = new IamStack(app, "cdktf")
    app.synth()
  }

}
