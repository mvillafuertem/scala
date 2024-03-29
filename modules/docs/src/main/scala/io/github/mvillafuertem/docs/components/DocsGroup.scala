package io.github.mvillafuertem.docs.components

import slinky.core._
import slinky.core.annotations.react
import slinky.reactrouter.NavLink
import slinky.web.html._

import scala.scalajs.js.Dynamic.literal

@react object DocsGroup {

  case class Props(name: String, isOpen: Boolean, children: List[(String, String)])

  val component = FunctionalComponent[Props] { props =>
    div(style := literal(width = "100%"))(
      button(
        style := literal(
          backgroundColor = "transparent",
          marginTop = "10px",
          border = "none",
          fontSize = "18px",
          textTransform = "uppercase",
          fontWeight = "700",
          padding = "0",
          width = "100%",
          textAlign = "left",
          outline = "none"
        )
      )(
        div(
          style := literal(
            color = if (props.isOpen) "rgb(26, 26, 26)" else "rgb(109, 109, 109)"
          )
        )(props.name)
      ),
      ul(style := literal(display = "block", listStyle = "none", padding = "0"))(
        props.children.zipWithIndex.map { case ((name, link), index) =>
          li(key := index.toString, style := literal(marginTop = "5px", marginBottom = "10px"))(
            NavLink(link, Some(literal(fontWeight = 700)), None)(
              style := literal(
                color = "rgb(26, 26, 26)",
                backgroundColor = "transparent",
                borderBottom = "none"
              )
            )(name)
          )
        }
      )
    )
  }
}
