lazy val commonSettings = Seq(
  organization := "io.github.mvillafuertem",
  version := "0.1",
  scalaVersion := "2.12.8"
)

lazy val advanced = (project in file("advanced"))
  .settings(commonSettings,
    name := "advanced",
    libraryDependencies ++= Dependencies.production,
    libraryDependencies ++= Dependencies.test
  )

lazy val akka = (project in file("akka"))
  .settings(commonSettings,
    name := "akka",
    libraryDependencies ++= Dependencies.production,
    libraryDependencies ++= Dependencies.test
  )

lazy val docs = (project in file("docs"))
  .enablePlugins(MicrositesPlugin, SitePlugin, SitePreviewPlugin)
  .settings(commonSettings,
    name := "docs",
    micrositeName := "Your Awesome Library Name",
    micrositeDescription := "This is the description of my Awesome Library",
    micrositeUrl := "http://mvillafuertem.io",
    micrositeBaseUrl := "/scala",
    micrositeDocumentationLabelDescription := "Documentation",
    micrositeAuthor := "mvillafuertem",
    micrositePalette := Map(
      "brand-primary"     -> "#E05236",
      "brand-secondary"   -> "#3F3242",
      "brand-tertiary"    -> "#2D232F",
      "gray-dark"         -> "#453E46",
      "gray"              -> "#837F84",
      "gray-light"        -> "#E3E2E3",
      "gray-lighter"      -> "#F4F3F4",
      "white-color"       -> "#FFFFFF")
  )