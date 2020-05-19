package io.github.mvillafuertem.slinky.components

import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{ div, onClick, style }
import typings.materialUiCore.{ components => Mui }

import scala.scalajs.js

@react class SlinkyButton extends Component {

  case class Props(name: String)

  case class State(n: Int)

  override def initialState = State(n = 1)

  def acc(): Unit = setState(state => state.copy(state.n + 1))

  override def render(): ReactElement =
    div(
      style := js.Dynamic.literal(
        fontSize = "30px"
      )
    )(
      Mui.TextField.StandardTextFieldProps(value = state.n.toString, disabled = true),
      Mui.Button(
        style := js.Dynamic.literal(
          fontSize = "20px",
          backgroundColor = "#234563",
          color = "#FFF"
        ),
        onClick := acc _
      )(s"Increment it, ${props.name}")
    )

}
