import sbt.Keys.{ commands, dependencyClasspathAsJars }
import sbt.{ Command, Compile, Project }

import scala.sys.process.Process

object Commands {

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
        value => value.map(_.data).filter(_.getPath.contains("h2database")).map(file => Process(s"java -jar ${file} org.h2.tools.Server").!).head
      )
    state
  }

  commands += h2Command

  val value = Seq(
    FmtSbtCommand,
    FmtSbtCheckCommand,
    h2Command
  )

}
