//package io.github.mvillafuertem.terraform.cdktf
//
//import org.scalajs.dom.console
//import org.scalatest.flatspec.AnyFlatSpecLike
//import org.scalatest.matchers.should.Matchers
//import typings.node.childProcessMod.{ExecException, exec}
//
//import scala.scalajs.js
//import scala.scalajs.js.|
//
//final class MyStackSpec extends AnyFlatSpecLike with Matchers {
//  self =>
//
//  behavior of s"${self.getClass.getSimpleName}"
//
//  it should "cdk.tf.json" in {
//
//    val process = exec("""echo "PEPEPEPEPEPEP"""", ((error, stdout, stderr) => {
//      console.log(s"stdout: ${stdout}");
//      console.error(s"stderr: ${stderr}");
//
//
//    }): js.Function3[/* error */ ExecException | Null, /* stdout */ String, /* stderr */ String, Unit])
//
//    //js.JSON.stringify(process) shouldBe "asdfasd"
//
////    for {
////      _ <- exec("sbt terraform-cdktf/npmInstallDependencies")
////      _ <- exec("""modules/terraform-cdktf/target/scala-2.13/scalajs-bundler/main/node_modules/cdktf-cli/bin/cdktf synth -a "sbt terraform-cdktf/run" \
////                  |--log-level=DEBUG
////                  |--json \
////                  |--output modules/terraform-cdktf/src/test/resources""".stripMargin)
////    } yield ()
//
//    // w h e n
//    //val inputStream = self.getClass.getResourceAsStream("/cdk.tf.json")
//    //val cdktf = scala.io.Source.fromInputStream(inputStream).getLines().mkString("\n")
//
//    // t h e n
//    //js.JSON.parse(CalendarCSS.asInstanceOf[String])
//    // https://stackoverflow.com/questions/40866662/unit-testing-scala-js-read-test-data-from-file-residing-in-test-resources
//    //root.`//`.metadata.stackName.string.getOption(json).fold(fail("Empty"))(a => a shouldBe "hello-terraformjkjk")
//    //    val assertion: Assertion = (for {
//    //      stackName <- root.`//`.metadata.stackName.string.getOption(json)
//    //      version <- root.`//`.metadata.version.string.getOption(json)
//    //      region <- root.provider.aws.region.string.getOption(json)
//    //    } yield (stackName, version, region)).fold(fail("")) { case (stackName, version, region) =>
//    //      stackName shouldBe "hello-terraform"
//    //      version shouldBe "0.0.18"
//    //      region shouldBe "us-east-1"
//    //    }
//
//  }
//
//}
//
//object MyStackSpec {}
