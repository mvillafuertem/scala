package io.github.mvillafuertem.aws

import software.amazon.awscdk.core.App

object CDKApp {
  def main(args: Array[String]): Unit = {
    val app = new App()

    new WebServerStack(app, "WebServerStack")

    app.synth()
  }
}