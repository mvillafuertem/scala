lazy val commonSettings = Settings.value ++ Seq(
  organization := "io.github.mvillafuertem",
  version := "0.1",
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

ThisBuild / scalaVersion := Settings.scala213

lazy val scala = (project in file("."))
  .aggregate(
    advanced,
    akka,
    algorithms,
    cats,
    docs
  )
  .settings(commonSettings)
  .settings(Settings.noPublish)
//  .settings(
//    // crossScalaVersions must be set to Nil on the aggregating project
//    crossScalaVersions := Nil
//  )

lazy val advanced = (project in file("advanced"))
  // S E T T I N G S
  .settings(scalaVersion := Settings.scala213)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.test)

lazy val akka = (project in file("akka"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  // S E T T I N G S
  .settings(scalaVersion := Settings.scala213)
  .settings(commonSettings)
  .settings(NexusSettings.value)
  .settings(libraryDependencies ++= Dependencies.akka)
  .settings(libraryDependencies ++= Dependencies.akkaTest)
  .settings(libraryDependencies ++= Dependencies.test)

lazy val algorithms = (project in file("algorithms"))
  // S E T T I N G S
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.test)

lazy val cats = (project in file("cats"))
  .dependsOn(algorithms)
  // S E T T I N G S
  .settings(scalaVersion := Settings.scala213)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Dependencies.cats)
  .settings(libraryDependencies ++= Dependencies.test)

lazy val docs = (project in file("docs"))
  // S E T T I N G S
  .settings(scalaVersion := Settings.scala212)
  .settings(commonSettings)
  .settings(MicrositeSettings.settings)
  // P L U G I N S
  .enablePlugins(MicrositesPlugin)
