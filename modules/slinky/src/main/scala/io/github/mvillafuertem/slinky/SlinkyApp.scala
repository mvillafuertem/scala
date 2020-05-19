package io.github.mvillafuertem.slinky

import org.scalajs.dom
import slinky.web.ReactDOM
import slinky.web.html._
import zio.{ App, ZIO }
import io.github.mvillafuertem.slinky.components._

object SlinkyApp extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    ZIO.succeed(ReactDOM.render(div(SlinkyButton("dear user")), dom.document.getElementById("root"))).as(0)

}
