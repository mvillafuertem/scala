import sbt.Keys._
import sbt.{ url, Developer, ScmInfo }

object Information {

  lazy val value = Seq(
    organization := "io.github.mvillafuertem",
    homepage := Some(url("https://github.com/mvillafuertem/scala")),
    licenses := List("MIT" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "mvillafuertem",
        "Miguel Villafuerte",
        "mvillafuertem@email.com",
        url("https://github.com/mvillafuertem")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/mvillafuertem/scala"),
        "scm:git@github.com:mvillafuertem/scala.git"
      )
    )
  )

}
