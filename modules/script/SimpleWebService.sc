#!/usr/bin/env amm

import $ivy.`com.lihaoyi::cask:0.7.11`

// amm ./SimpleWebService.sc &
@main
def main(): Unit = WebApp.main(Array())

object WebApp extends cask.MainRoutes {

  @cask.get("/")
  def hello(): String =
    "Hello World!"

  @cask.post("/", subpath = true)
  def doThing(request: cask.Request): Unit =
    println(request.text())

  override val host: String = "0.0.0.0"
  override def port: Int    = 8089

  initialize()
}
