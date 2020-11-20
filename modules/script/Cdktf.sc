import $ivy.`com.lihaoyi::ammonite-ops:2.2.0`

import ammonite.ops._

@main
def main(): Int = {
  %.sbt("terraform-cdktf/npmInstallDependencies")(os.pwd)
  0
}