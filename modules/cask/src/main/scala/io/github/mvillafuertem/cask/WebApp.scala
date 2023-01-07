package io.github.mvillafuertem.cask

//sbt "project cask;run"
object WebApp extends cask.MainRoutes {

  @cask.route("/", methods = Seq("get", "post", "put", "delete", "patch"), subpath = true)
  def doThing(request: cask.Request): String = {
    println(request.toString)
    println(request.headers)
    println(request.text())
    "Hello World!"
  }

  override val host: String = "0.0.0.0"

  initialize()
}
