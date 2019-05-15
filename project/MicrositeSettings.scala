import microsites.ExtraMdFileConfig
import microsites.MicrositesPlugin.autoImport._
import sbt._

object MicrositeSettings {

  val settings: Seq[Def.Setting[_]] = Seq(
    micrositeName := "scala",
    micrositeDescription := "Scala",
    micrositeFooterText := Some(
      """
         <p>© 2019 <a href="">The mvillafuertem Maintainers</a></p>
         <p style="font-size: 80%; margin-top: 10px">Website built with <a href="https://47deg.github.io/sbt-microsites/">sbt-microsites © 2016 47 Degrees</a></p>
      """.stripMargin
    ),
    micrositeHighlightTheme := "atom-one-light",
    micrositeHomepage := "http://typelevel.org/cats/",
    micrositeUrl := "http://mvillafuertem.io",
    micrositeBaseUrl := "/scala",
    // D O C U M E N T A T I O N
    // micrositeDocumentationUrl := "/cats/api/cats/index.html",
    // micrositeDocumentationLabelDescription := "Documentation",
    micrositeAuthor := "mvillafuertem",
    micrositeExtraMdFiles := Map(
      file("README.md") -> ExtraMdFileConfig(
        "index.md",
        "home",
        Map("title" -> "Home", "section" -> "home", "position" -> "0")
      )
    ),
    micrositePalette := Map(
      "brand-primary" -> "#E05236",
      "brand-secondary" -> "#3F3242",
      "brand-tertiary" -> "#2D232F",
      "gray-dark" -> "#453E46",
      "gray" -> "#837F84",
      "gray-light" -> "#E3E2E3",
      "gray-lighter" -> "#F4F3F4",
      "white-color" -> "#FFFFFF")
  )
}
