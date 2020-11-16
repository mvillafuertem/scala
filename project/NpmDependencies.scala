object NpmDependencies {

  lazy val `terraform-cdktf`: Seq[(String, String)] = Seq(
    Package.`@cdktf/provider-aws` -> Version.`@cdktf/provider-aws`,
    Package.`cdktf`               -> Version.`cdktf`,
    Package.`cdktf-cli`           -> Version.`cdktf-cli`,
    Package.`constructs`          -> Version.`constructs`,
    Package.`node`                -> Version.`node`,
    Package.`@types/node`         -> Version.`@types/node`
  )

  lazy val `dev-terraform-cdktf`: Seq[(String, String)] = Seq(
    Package.`file-loader`           -> Version.`file-loader`,
    Package.`style-loader`          -> Version.`style-loader`,
    Package.`css-loader`            -> Version.`css-loader`,
    Package.`html-webpack-plugin`   -> Version.`html-webpack-plugin`,
    Package.`copy-webpack-plugin`   -> Version.`copy-webpack-plugin`,
    Package.`webpack-merge`         -> Version.`webpack-merge`,
    Package.`terser-webpack-plugin` -> Version.`terser-webpack-plugin`
  )

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
    val `@cdktf/provider-aws`      = "@cdktf/provider-aws"
    val `@material-ui/core`        = "@material-ui/core"
    val `@material-ui/styles`      = "@material-ui/styles"
    val `@types/node`              = "@types/node"
    val `@types/react`             = "@types/react"
    val `@types/react-dom`         = "@types/react-dom"
    val `cdktf`                    = "cdktf"
    val `cdktf-cli`                = "cdktf-cli"
    val `constructs`               = "constructs"
    val `copy-webpack-plugin`      = "copy-webpack-plugin"
    val `css-loader`               = "css-loader"
    val `file-loader`              = "file-loader"
    val `html-webpack-plugin`      = "html-webpack-plugin"
    val `node`                     = "node"
    val `react`                    = "react"
    val `react-dom`                = "react-dom"
    val `react-helmet`             = "react-helmet"
    val `react-proxy`              = "react-proxy"
    val `react-router-dom`         = "react-router-dom"
    val `react-syntax-highlighter` = "react-syntax-highlighter"
    val `remark`                   = "remark"
    val `remark-react`             = "remark-react"
    val `style-loader`             = "style-loader"
    val `terser-webpack-plugin`    = "terser-webpack-plugin"
    val `webpack-merge`            = "webpack-merge"
  }

  private object Version {
    val `@cdktf/provider-aws`      = "0.0.72"
    val `@material-ui/core`        = "3.9.3"          // note: version 4 is not supported yet
    val `@material-ui/styles`      = "3.0.0-alpha.10" // note: version 4 is not supported yet
    val `@types/node`              = "14.14.7"
    val `@types/react`             = "16.9.34"
    val `@types/react-dom`         = "16.9.6"
    val `cdktf`                    = "0.0.18"
    val `cdktf-cli`                = "0.0.18"
    val `constructs`               = "3.2.30"
    val `copy-webpack-plugin`      = "5.1.1"
    val `css-loader`               = "3.5.3"
    val `file-loader`              = "6.0.0"
    val `html-webpack-plugin`      = "4.3.0"
    val `node`                     = "15.2.0"
    val `react`                    = "16.13.1"
    val `react-dom`                = "16.13.1"
    val `react-helmet`             = "5.2.0"
    val `react-proxy`              = "1.1.8"
    val `react-router-dom`         = "5.2.0"
    val `react-syntax-highlighter` = "6.0.4"
    val `remark`                   = "8.0.0"
    val `remark-react`             = "4.0.1"
    val `style-loader`             = "1.2.1"
    val `terser-webpack-plugin`    = "5.0.3"
    val `webpack-merge`            = "4.2.2"
  }

}
