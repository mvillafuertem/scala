#!/usr/bin/env amm

// amm ./SimpleCLI.sc
@main
def main(args: Array[String]): Unit = {
  case class AppArgs(
                      seed1: String,
                      seed2: String,
                      ip: String,
                      port: Int
                    )
  object AppArgs {
    def empty = new AppArgs("", "", "", 0)
  }

  val args = Array[String](
    "--seed1", "akka.tcp://seed1",
    "--seed2", "akka.tcp://seed2",
    "--nodeip", "192.167.1.1",
    "--nodeport", "2551"
  )

  val argsInstance = args.sliding(2, 2).toList.foldLeft(AppArgs.empty) { case (accumArgs, currArgs) => currArgs match {
    case Array("--seed1", seed1) => accumArgs.copy(seed1 = seed1)
    case Array("--seed2", seed2) => accumArgs.copy(seed2 = seed2)
    case Array("--nodeip", ip) => accumArgs.copy(ip = ip)
    case Array("--nodeport", port) => accumArgs.copy(port = port.toInt)
    case unknownArg => accumArgs // Do whatever you want for this case
  }
  }
}

// Original POC
//var name = ""
//var port = 0
//var ip = ""
//args.sliding(2, 2).toList.collect {
//  case Array("--ip", argIP: String) => ip = argIP
//  case Array("--port", argPort: String) => port = argPort.toInt
//  case Array("--name", argName: String) => name = argName
//}