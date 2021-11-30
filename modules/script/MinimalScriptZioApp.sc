#!/usr/bin/env amm

import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`com.lihaoyi::mainargs:0.2.1`
import $ivy.`dev.zio::zio-logging-slf4j:0.5.12`
import $ivy.`dev.zio::zio:1.0.11`

import MinimalScriptZioApp.Config
import mainargs.{ arg, main, Flag, ParserForClass }
import zio.logging.log
import zio.logging.slf4j.Slf4jLogger
import zio.{ ExitCode, Task, URIO, ZEnv }

@main(
  name = "minimal-script-zio-app",
  doc = ""
)
def minimalScriptZioApp(config: Config): Unit =
  MinimalScriptZioApp.main(Array(config.foo, config.myNum.toString, config.bool.toString))

object MinimalScriptZioApp extends zio.App {

  val loggerLayer = Slf4jLogger.make((_, message) => message)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = (for {
    _ <- Task().tap(_ => log.info("Start"))
    _ <- log.info("Press Any Key to stop the demo server")
  } yield ())
    .catchAll(e => log.error(s"${e.getClass.getName} : ${e.getMessage}"))
    .provideSomeLayer[ZEnv](loggerLayer)
    .exitCode

  // Configuration
  implicit def configParser: ParserForClass[Config] = ParserForClass[Config]
  case class Config(
    @arg(short = 'f', doc = "String to print repeatedly")
    foo: String,
    @arg(name = "my-num", doc = "How many times to print string")
    myNum: Int = 2,
    @arg(doc = "Example flag")
    bool: Flag
  )
}
