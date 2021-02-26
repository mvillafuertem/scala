object NpmDependencies {

  lazy val slinky: Seq[(String, String)] = Seq(
    Package.`react`               -> Version.`react`,
    Package.`react-dom`           -> Version.`react-dom`,
    Package.`@types/react`        -> Version.`@types/react`,
    Package.`@types/react-dom`    -> Version.`@types/react-dom`,
    Package.`@material-ui/core`   -> Version.`@material-ui/core`,
    Package.`@material-ui/styles` -> Version.`@material-ui/styles`
  )

  lazy val docs: Seq[(String, String)] = Seq(
    Package.`react`                    -> Version.`react`,
    Package.`react-dom`                -> Version.`react-dom`,
    Package.`react-router-dom`         -> Version.`react-router-dom`,
    Package.`react-proxy`              -> Version.`react-proxy`,
    Package.`remark`                   -> Version.`remark`,
    Package.`remark-react`             -> Version.`remark-react`,
    Package.`react-helmet`             -> Version.`react-helmet`,
    Package.`react-syntax-highlighter` -> Version.`react-syntax-highlighter`
  )

  private object Package {
    val `@material-ui/core`        = "@material-ui/core"
    val `@material-ui/styles`      = "@material-ui/styles"
    val `@types/node`              = "@types/node"
    val `@types/react`             = "@types/react"
    val `@types/react-dom`         = "@types/react-dom"
    val `react`                    = "react"
    val `react-dom`                = "react-dom"
    val `react-helmet`             = "react-helmet"
    val `react-proxy`              = "react-proxy"
    val `react-router-dom`         = "react-router-dom"
    val `react-syntax-highlighter` = "react-syntax-highlighter"
    val `remark`                   = "remark"
    val `remark-react`             = "remark-react"
  }

  private object Version {
    val `@material-ui/core`        = "3.9.3"          // note: version 4 is not supported yet
    val `@material-ui/styles`      = "3.0.0-alpha.10" // note: version 4 is not supported yet
    val `@types/node`              = "14.14.7"
    val `@types/react`             = "16.9.34"
    val `@types/react-dom`         = "16.9.6"
    val `react`                    = "16.13.1"
    val `react-dom`                = "16.13.1"
    val `react-helmet`             = "5.2.0"
    val `react-proxy`              = "1.1.8"
    val `react-router-dom`         = "5.2.0"
    val `react-syntax-highlighter` = "6.0.4"
    val `remark`                   = "8.0.0"
    val `remark-react`             = "4.0.1"
  }

}
