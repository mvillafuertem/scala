import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.dist
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.fullOptJS
import sbt.Keys._
import sbt.io.RichFile
import sbt.{ Compile, File, IO, Project, ThisBuild }
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.webpack

object WebpackSettings {

  /**
   * Implement the `dist` task defined above.
   * Most of this is really just to copy the index.html file around.
   */
  lazy val browserProject: Project => Project =
    _.settings(
      dist := {
        val artifacts        = (Compile / fullOptJS / webpack).value
        val artifactFolder   = (Compile / fullOptJS / crossTarget).value
        val distFolder: File = new RichFile((ThisBuild / baseDirectory).value) / "docs"

        distFolder.mkdirs()
        artifacts.foreach { artifact =>
          val target = new RichFile(artifact.data).relativeTo(artifactFolder) match {
            case None          => new RichFile(distFolder) / new RichFile(artifact.data).name
            case Some(relFile) => new RichFile(distFolder) / relFile.toString
          }

          Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
        }

        val indexFrom = new RichFile(baseDirectory.value) / "src/main/js/index.html"
        val indexTo   = new RichFile(distFolder) / "index.html"

        val indexPatchedContent = {
          import collection.JavaConverters._
          Files
            .readAllLines(indexFrom.toPath, IO.utf8)
            .asScala
            .map(_.replaceAllLiterally("-fastopt-", "-opt-"))
            .mkString("\n")
        }

        Files.write(indexTo.toPath, indexPatchedContent.getBytes(IO.utf8))
        distFolder
      }
    )

}
