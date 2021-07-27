package io.github.mvillafuertem.graalvm.cli

import picocli.CommandLine
import picocli.CommandLine.{ Command, Option, Parameters }

import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.Callable

@Command(
  name = "checksum",
  mixinStandardHelpOptions = true,
  version = Array("checksum 4.0"),
  description = Array("Prints the checksum (MD5 by default) of a file to STDOUT.")
)
class CliApp extends Callable[Int] {

  @Parameters(
    index = "0",
    description = Array("The file whose checksum to calculate.")
  )
  private var file: File = null

  @Option(
    names = Array("-a", "--algorithm"),
    description = Array("MD5, SHA-1, SHA-256, ...")
  )
  private var algorithm = "MD5"

  override def call(): Int = {
    val fileContents = Files.readAllBytes(file.toPath)
    val digest       = MessageDigest.getInstance(algorithm).digest(fileContents)
    printf("%0" + (digest.length * 2) + "x%n", new BigInteger(1, digest))
    0
  }
}

object CliApp extends App {
  val exitCode = new CommandLine(new CliApp()).execute(args: _*)
  System.exit(exitCode)
}
