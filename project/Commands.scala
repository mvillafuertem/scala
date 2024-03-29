import sbt.Keys.dependencyClasspathAsJars
import sbt.{ Command, Project, Test }

import scala.sys.process.Process

object Commands {

  val DocsDevCommand = Command.command("docs")(state => "project docs" :: "fastOptJS::startWebpackDevServer" :: "~fastOptJS" :: state)

  def frontendDevCommand(nameOfProject: String): Command =
    Command.command(s"$nameOfProject")(state => s"project $nameOfProject" :: "fastOptJS::startWebpackDevServer" :: "~fastOptJS" :: state)

  val FrontendBuildCommand = Command.command("build")(state => "project slinky" :: "fullOptJS::webpack" :: state)

  val FmtSbtCommand = Command.command("fmt")(state => "scalafmtSbt" :: "scalafmt" :: "test:scalafmt" :: state)

  val FmtSbtCheckCommand = Command.command("check")(state => "scalafmtSbtCheck" :: "scalafmtCheck" :: "test:scalafmtCheck" :: state)

  val h2Command = Command.command("h2Console") { state =>
    getClasspathAsJars(state).filter(_.contains("h2database")).map(file => Process(s"java -jar ${file} -browser").!).head
    state
  }

  val ammoniteCommand = Command.args("amm", "<scriptClass>") { (state, args) =>
    val cp = getClasspathAsJars(state).mkString(":")
    Process(s"java -classpath ${cp} ammonite.Main ${args.mkString(" ")}").!
    state
  }

  val stcCommand = Command.args("cdktf", "<args>") { (state, args) =>
    val cp = getClasspathAsJars(state).mkString(":")
    Process(s"java -classpath ${cp} org.scalablytyped.converter.cli.Main ${args.mkString(" ")}").!
    state
  }

  def getClasspathAsJars(state: sbt.State) = Project
    .runTask(Test / dependencyClasspathAsJars, state)
    .get
    ._2
    .toEither
    .fold(
      exception => throw exception,
      value => value.map(_.data.getPath)
    )

  val value = Seq(
    DocsDevCommand,
    FrontendBuildCommand,
    FmtSbtCommand,
    FmtSbtCheckCommand
  )

}
