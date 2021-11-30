package io.github.mvillafuertem.docs

import io.github.mvillafuertem.docs.components.{ DocsPage, Navbar }
import org.scalajs.dom
import slinky.history.History
import slinky.reactrouter.{ Route, Router, Switch }
import slinky.web.ReactDOM
import slinky.web.html._
import zio.{ App, ExitCode, ZIO }

import scala.scalajs.js

object DocsApp extends App {

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO
      .succeed(
        ReactDOM.render(
          Router(History.createBrowserHistory())(
            div(
              Navbar(()),
              div(
                style := js.Dynamic.literal(
                  marginTop = "60px"
                )
              )(
                Switch(
                  Route("/docs/*", DocsPage.component)
                )
              )
            )
          ),
          dom.document.getElementById("container")
        )
      )
      .exitCode

}
