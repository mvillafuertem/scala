lazy val commonSettings = Seq(
  organization := "io.github.mvillafuertem",
  version := "0.1",
  scalaVersion := "2.12.8"
)

lazy val advanced = (project in file("advanced"))
  .settings(commonSettings,
    name := "advanced",
    libraryDependencies ++= Dependencies.production
  )

lazy val akka = (project in file("akka"))
  .settings(commonSettings,
    name := "akka",
    libraryDependencies ++= Dependencies.production,
    libraryDependencies ++= Dependencies.test
  )