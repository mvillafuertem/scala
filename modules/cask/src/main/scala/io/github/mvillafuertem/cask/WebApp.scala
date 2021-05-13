package io.github.mvillafuertem.cask

//sbt "project cask;run"
object WebApp extends cask.MainRoutes {

  @cask.get("/")
  def hello(): String =
    "Hello World!"

  @cask.post("/", subpath = true)
  def doThing(request: cask.Request): Unit =
    println(request.text())

  override val host: String = "0.0.0.0"

  initialize()
}
