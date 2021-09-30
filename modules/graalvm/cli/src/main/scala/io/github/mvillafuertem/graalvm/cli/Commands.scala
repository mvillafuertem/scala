package io.github.mvillafuertem.graalvm.cli

import picocli.CommandLine.Model.CommandSpec

object Commands {

  val login: CommandSpec = CommandSpec
    .create
    .name("login")

}
