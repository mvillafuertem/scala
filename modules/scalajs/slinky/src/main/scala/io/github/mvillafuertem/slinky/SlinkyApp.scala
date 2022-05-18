package io.github.mvillafuertem.slinky

import io.github.mvillafuertem.slinky.components._
import org.scalajs.dom
import slinky.web.ReactDOM
import slinky.web.html._
import zio.{ App, ExitCode, ZIO }

// yarn --cwd modules/slinky/ install
// sbt "project slinky; ~fastLinkJS"
// yarn --cwd modules/slinky/ dev (in a separate terminal)
object SlinkyApp extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO
      .succeed(ReactDOM.render(div(SlinkyButton("dear user")), dom.document.getElementById("root")))
      .exitCode

}
