#!/usr/bin/env amm

import java.io.{File, FileInputStream, PrintWriter}
import java.nio.charset.StandardCharsets

import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`dev.zio::zio:1.0.0-RC18-2`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import org.slf4j.{Logger, LoggerFactory}
import zio.console._
import zio.{Task, UIO, URIO}

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// amm `pwd`/ReadWriteFile.sc
@main
def main(): Unit = ReadWriteFile.main(Array())

object ReadWriteFile extends zio.App {

  def writeFile(path: String, body: String) =
    Task
      .effect(new PrintWriter(new File(path)))
      .bracket(pw => UIO.effectTotal(pw.close()))(pw => Task.effect(pw.write(body)))

  def readFile(path: String): Task[FileInputStream] =
    Task.effect(new FileInputStream(new File(path)))

  def closeFile(is: FileInputStream): UIO[Unit] =
    UIO.effectTotal(is.close())

  def printResult(is: FileInputStream) =
    Task.effect(is.readAllBytes()).flatMap(bytes => putStrLn(new String(bytes, StandardCharsets.UTF_8)))

  def program =
    writeFile("/tmp/hello", "Hello World")
      .onError(ex => putStrLn(s"Failed to write file: ${ex.failures}")) *>
      readFile("/tmp/hello")
        .bracket(closeFile)(printResult)
        .onError(ex => putStrLn(s"Failed to read file: ${ex.failures}"))

  override def run(args: List[String]): URIO[Console, Int] =
    program.fold(_ => 1, _ => 0)

}