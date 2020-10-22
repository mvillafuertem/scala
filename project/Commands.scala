import sbt.Keys.{ dependencyClasspathAsJars }
import sbt.{ Command, Compile, Project, Test }

import scala.sys.process.Process

object Commands {

  val DocsDevCommand = Command.command("docs")(state => "project docs" :: "fastOptJS::startWebpackDevServer" :: "~fastOptJS" :: state)

  val FrontendDevCommand = Command.command("dev")(state => "project slinky" :: "fastOptJS::startWebpackDevServer" :: "~fastOptJS" :: state)

  val FrontendBuildCommand = Command.command("build")(state => "project slinky" :: "fullOptJS::webpack" :: state)

  val FmtSbtCommand = Command.command("fmt")(state => "scalafmtSbt" :: "scalafmt" :: "test:scalafmt" :: state)

  val FmtSbtCheckCommand = Command.command("check")(state => "scalafmtSbtCheck" :: "scalafmtCheck" :: "test:scalafmtCheck" :: state)

  val h2Command = Command.command("h2Console") { state =>
    Project
      .runTask(Compile / dependencyClasspathAsJars, state)
      .get
      ._2
      .toEither
      .fold(
        exception => exception.printStackTrace(),
        value => value.map(_.data).filter(_.getPath.contains("h2database")).map(file => Process(s"java -jar ${file} -browser").!).head
      )
    state
  }

  val ammoniteCommand = Command.args("amm", "<scriptClass>") { (state, args) =>
    val cp = Project
      .runTask(Test / dependencyClasspathAsJars, state)
      .get
      ._2
      .toEither
      .fold(
        exception => throw exception,
        value => value.map(_.data.getPath)
      )
      .mkString(":")

    Process(s"java -classpath ${cp} ammonite.Main ${args.mkString(" ")}").!

    state
  }

  val value = Seq(
    DocsDevCommand,
    FrontendDevCommand,
    FrontendBuildCommand,
    FmtSbtCommand,
    FmtSbtCheckCommand
  )

}
