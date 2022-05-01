object NpmDependencies {

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
