lazy val commonSettings = Settings.value ++ Seq(
  organization := "io.github.mvillafuertem",
  version := "0.1",
  scalaVersion := "2.12.8",
  homepage := Some(url("https://github.com/mvillafuertem/scala")),
  licenses := List("MIT" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "mvillafuertem",
      "Miguel Villafuerte",
      "mvillafuertem@email.com",
      url("https://github.com/mvillafuertem")
    )
  )
)

lazy val scala = (project in file("."))
  .aggregate(
    advanced,
    akka,
    algorithms,
    cats,
    docs
  )
  .settings(commonSettings)

lazy val advanced = (project in file("advanced"))
  .settings(commonSettings,
    libraryDependencies ++= Dependencies.test
  )

lazy val akka = (project in file("akka"))
  .settings(commonSettings,
    libraryDependencies ++= Dependencies.akka,
    libraryDependencies ++= Dependencies.akkaTest,
    libraryDependencies ++= Dependencies.test
  )

lazy val algorithms = (project in file("algorithms"))
  .settings(commonSettings,
    libraryDependencies ++= Dependencies.test
  )

lazy val cats = (project in file("cats"))
  .dependsOn(algorithms)
  .settings(commonSettings,
    libraryDependencies ++= Dependencies.cats,
    libraryDependencies ++= Dependencies.test
  )

lazy val docs = (project in file("docs"))
  .enablePlugins(MicrositesPlugin)
  .settings(commonSettings, 
    MicrositeSettings.settings)