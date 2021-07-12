import com.typesafe.sbt.SbtNativePackager.autoImport.packageName
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{ dockerUpdateLatest, Docker }
import com.typesafe.sbt.packager.graalvmnativeimage.GraalVMNativeImagePlugin.autoImport.graalVMNativeImageOptions
import sbt.Keys._
import sbt.{ Compile, Logger, settingKey, taskKey, _ }

import java.nio.file.{ Files, StandardCopyOption }

object GraalVMSettings {
  val processAnnotations           = taskKey[Unit]("Process annotations")
  val dockerGraalvmNative          = taskKey[Unit]("Create a docker image containing a binary build with GraalVM's native-image.")
  val dockerGraalvmNativeImageName = settingKey[String]("Name of the generated docker image, containing the native binary.")

  private def failIfNonZeroExitStatus(command: String, message: => String, log: Logger): Unit = {
    import scala.sys.process._
    val result = command !

    if (result != 0) {
      log.error(message)
      sys.error("Failed running command: " + command)
    }
  }

  lazy val value: Seq[Def.Setting[_]] = Seq(
    processAnnotations := {
      val log = streams.value.log

      log.info("Processing annotations ...")

      val classpath            = ((Compile / products).value ++ ((Compile / dependencyClasspath).value.files)) mkString ":"
      val destinationDirectory = (Compile / classDirectory).value
      val processor            = "picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor"
      val classesToProcess     = Seq((Compile / mainClass).value.getOrElse(sys.error("Could not find a main class."))) mkString " "

      val command = s"javac -cp $classpath -proc:only -processor $processor -XprintRounds -d $destinationDirectory $classesToProcess"

      failIfNonZeroExitStatus(command, "Failed to process annotations.", log)

      log.info("Done processing annotations.")
    },
    Compile / packageBin := (Compile / packageBin dependsOn (Compile / processAnnotations)).value,
    Docker / packageName := "docker-test",
    dockerUpdateLatest := true,
    dockerGraalvmNativeImageName := "docker-graalvm-native-test",
    dockerGraalvmNative := {
      val log = streams.value.log

      val stageDir = target.value / "native-docker" / "stage"
      stageDir.mkdirs()

      // copy all jars to the staging directory
      val cpDir = stageDir / "cp"
      cpDir.mkdirs()

      val classpathJars = Seq((Compile / packageBin dependsOn (Compile / processAnnotations)).value) ++ (Compile / dependencyClasspath).value.map(_.data)
      classpathJars.foreach(cpJar => Files.copy(cpJar.toPath, (cpDir / cpJar.name).toPath, StandardCopyOption.REPLACE_EXISTING))

      val resultDir  = stageDir / "result"
      resultDir.mkdirs()
      val resultName = "out"

      val className = (Compile / mainClass).value.getOrElse(sys.error("Could not find a main class."))

      val runNativeImageCommand = Seq(
        "docker",
        "run",
        "--rm",
        "-v",
        s"${cpDir.getAbsolutePath}:/opt/cp",
        "-v",
        s"${resultDir.getAbsolutePath}:/opt/graalvm",
        "graalvm-native-image",
        "-cp",
        "/opt/cp/*",
        "--static",
        // "--report-unsupported-elements-at-runtime",
        s"-H:Name=$resultName",
        className
      )

      log.info("Running native-image using the 'graalvm-native-image' docker container")
      log.info(s"Running: ${runNativeImageCommand.mkString(" ")}")

      sys.process.Process(runNativeImageCommand, resultDir) ! streams.value.log match {
        case 0 => resultDir / resultName
        case r => sys.error(s"Failed to run docker, exit status: " + r)
      }

      val buildContainerCommand = Seq(
        "docker",
        "build",
        "-t",
        dockerGraalvmNativeImageName.value,
        "-f",
        (baseDirectory.value.getParentFile / "run-native-image" / "Dockerfile").getAbsolutePath,
        resultDir.absolutePath
      )
      log.info("Building the container with the generated native image")
      log.info(s"Running: ${buildContainerCommand.mkString(" ")}")

      sys.process.Process(buildContainerCommand, resultDir) ! streams.value.log match {
        case 0 => resultDir / resultName
        case r => sys.error(s"Failed to run docker, exit status: " + r)
      }

      log.info(s"Build image ${dockerGraalvmNativeImageName.value}")
    },
    graalVMNativeImageOptions := Seq("--report-unsupported-elements-at-runtime")
  )

}
