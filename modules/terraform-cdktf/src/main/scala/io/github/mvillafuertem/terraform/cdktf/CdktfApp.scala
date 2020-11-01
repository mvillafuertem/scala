package io.github.mvillafuertem.terraform.cdktf

import typings.cdktf.mod.App
// 1. cdktf get
// 2. sbt terraform-cdktf/run
// 3. mv `pwd`/modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/main `pwd`/modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf
// 4. tsc --declaration --project modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf
// 6. cp -R `pwd`/modules/terraform/cdktf/.gen/ `pwd`/modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf/node_modules/scalajs-cdktf
// 7. sbt "project stc;stc -d modules/terraform/cdktf/target/scala-2.13/scalajs-bundler/scalajs-cdktf --includeProject=true"
// 8. cdktf synth -a "sbt terraform-cdktf/run" --log-level=DEBUG --json --output modules/terraform-cdktf/src/test/resources
object CdktfApp {

  def main(args: Array[String]): Unit = {

    val app = new App()
    new MyStack(app, "hello-terraform")
    app.synth()

  }

}
