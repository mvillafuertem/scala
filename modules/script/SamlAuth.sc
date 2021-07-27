import $ivy.`ch.qos.logback:logback-classic:1.2.4`
import $ivy.`com.softwaremill.sttp.tapir::tapir-akka-http-server:0.17.20`
import $ivy.`com.softwaremill.sttp.tapir::tapir-core:0.17.20`
import $ivy.`com.softwaremill.sttp.tapir::tapir-json-circe:0.17.20`
import $ivy.`com.softwaremill.sttp.tapir::tapir-openapi-circe-yaml:0.17.20`
import $ivy.`com.softwaremill.sttp.tapir::tapir-openapi-docs:0.17.20`
import $ivy.`com.softwaremill.sttp.tapir::tapir-swagger-ui-akka-http:0.17.20`
import $ivy.`dev.zio::zio:1.0.10`
import $ivy.`org.scala-lang.modules::scala-xml:2.0.0`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import $ivy.`software.amazon.awssdk:sts:2.16.83`

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.{ extractLog, extractRequestContext, withRequestTimeoutResponse }
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import org.slf4j.{ Logger, LoggerFactory }
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.{ AssumeRoleWithSamlRequest, AssumeRoleWithSamlResponse }
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.{ endpoint, htmlBodyUtf8, stringBody }
import zio.console._
import zio.{ Has, Runtime, Task, UIO, ZEnv, ZLayer, ZManaged }

import java.awt.Desktop
import java.io.{ File, FileInputStream, PrintWriter }
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// amm `pwd`/modules/script/SamlAuth.sc
@main
def main(): Unit = SamlAuthApplication.main(Array())

case class User(id: String, email: String, roles: Map[String, String])

object SamlAuthApplication extends zio.App {

  def writeFile(path: String, body: String) =
    Task
      .effect(new PrintWriter(new File(path)))
      .bracket(pw => UIO.effectTotal(pw.close()))(pw => Task.effect(pw.write(body)))

  def readFile(path: String): Task[FileInputStream] =
    Task.effect(new FileInputStream(new File(path)))

  def closeFile(is: FileInputStream): UIO[Unit] =
    UIO.effectTotal(is.close())

  def printResult(is: FileInputStream) =
    Task.effect(is.readAllBytes()).flatMap(bytes => putStrLn(new String(bytes, StandardCharsets.UTF_8)))

  def program =
    writeFile("/tmp/hello", "Hello World")
      .onError(ex => putStrLn(s"Failed to write file: ${ex.failures}").exitCode) *>
      readFile("/tmp/hello")
        .bracket(closeFile)(printResult)
        .onError(ex => putStrLn(s"Failed to read file: ${ex.failures}").exitCode)

  override def run(args: List[String]) =
    AkkaHttpServer.live.build.useForever.provideSomeLayer[ZEnv](Routes.live).exitCode

}

object SamlAuth {

  def doThing(request: String): Future[Either[Unit, String]] = {
    val region: String             = "eu-west-1"
    val roleArn: String            = "arn:aws:iam::XXXXXXXXXXXX:role/XXXXXX"
    val durationSeconds: Int       = 60 * 60 * 2 // 2 hours
    val samlAssertion: String      = parseRequestToSamlAssertion(request)
    val user: User                 = parseSamlAssertionToUser(samlAssertion)
    val assumeRoleWithSamlResponse = assumeRoleWithSaml(user, region, samlAssertion, roleArn, durationSeconds)
    println(assumeRoleWithSamlResponse.credentials())
    Future(
      Right(
        """
          |<!DOCTYPE html>
          |<html>
          |<head>
          |    <meta charset="utf-8" />
          |    <meta http-equiv="refresh" content="10;url=https://console.aws.amazon.com/console/home">
          |    <title>Login successfully</title>
          |</head>
          |<body>
          |    <h4>You have logged into Amazon Web Services!</h4>
          |    <p>You can close this window, or we will redirect you to the <a href="https://console.aws.amazon.com/console/home">Amazon Web Service Console</a> in 10 seconds.</p>
          |</body>
          |</html>
          |""".stripMargin
      )
    )
  }

  def assumeRoleWithSaml(user: User, region: String, samlAssertion: String, roleArn: String, durationSeconds: Int): AssumeRoleWithSamlResponse = {
    val principalArn              = user.roles(roleArn)
    val stsClient                 = StsClient
      .builder()
      .credentialsProvider(AnonymousCredentialsProvider.create())
      .region(Region.of(region))
      .build()
    val assumeRoleWithSamlRequest = AssumeRoleWithSamlRequest
      .builder()
      .roleArn(roleArn)
      .principalArn(principalArn)
      .samlAssertion(new String(Base64.getEncoder.encode(samlAssertion.getBytes()), StandardCharsets.UTF_8))
      .durationSeconds(durationSeconds)
      .build()
    stsClient.assumeRoleWithSAML(assumeRoleWithSamlRequest)
  }

  def parseRequestToSamlAssertion(request: String): String = {
    val unquote: String = java.net.URLDecoder.decode(request.split('=')(1), StandardCharsets.UTF_8)
    new String(Base64.getDecoder.decode(unquote), StandardCharsets.UTF_8)
  }

  def parseSamlAssertionToUser(samlAssertion: String): User = {
    val doc                 = scala.xml.XML.loadString(samlAssertion)
    val values: Seq[String] = (doc \\ "Attribute" \ "AttributeValue").map(_.text.trim)
    val roles               = values
      .filter(_.contains("arn"))
      .map(_.split(','))
      .map(roles => roles(0) -> roles(1))
      .foldLeft(Map.empty[String, String]) { case (map, (key, value)) => map + (key -> value) }
    User(values.head, values(2), roles)
  }
}

object Routes         extends Routes
object AkkaHttpServer extends AkkaHttpServer

trait Routes {

  val serverEndpoint: ServerEndpoint[String, Unit, String, Any, Future] = endpoint.post
    .in(stringBody)
    .out(htmlBodyUtf8)
    .serverLogic(SamlAuth.doThing)

  val live: ZLayer[zio.ZEnv, Throwable, Has[Route]] =
    ZManaged
      .runtime[zio.ZEnv]
      .map { implicit runtime: zio.Runtime[zio.ZEnv] =>
        withRequestTimeoutResponse(request =>
          HttpResponse(StatusCodes.EnhanceYourCalm, entity = "Unable to serve response within time limit, please enhance your calm.")
        ) {
          val routes: Route =
            extractLog { log =>
              extractRequestContext { requestContext =>
                log.info(s"extractRequestContext ~> ${requestContext.request}")
                AkkaHttpServerInterpreter.toRoute(serverEndpoint)
              }
            }
          routes
        }
      }
      .toLayer
}

trait AkkaHttpServer {

  private val log = LoggerFactory.getLogger(getClass)

  val interface = "localhost"
  val port: Int = 8407

  val live: ZLayer[ZEnv with Has[Route], Throwable, Has[
    Future[Http.ServerBinding]
  ]] =
    ZLayer.fromServiceManaged[
      Route,
      ZEnv,
      Throwable,
      Future[Http.ServerBinding]
    ](make)

  def make(
            routes: Route
          ): ZManaged[ZEnv, Throwable, Future[Http.ServerBinding]] =
    ZManaged.runtime[ZEnv].flatMap { implicit runtime: Runtime[ZEnv] =>
      ZManaged.makeEffect {
        implicit lazy val untypedSystem: ActorSystem = ActorSystem("saml-auth")
        implicit lazy val materializer: Materializer = Materializer(untypedSystem)
        Http().newServerAt(interface, port).bind(routes)
      }(_.map(_.terminate(10.second))(runtime.platform.executor.asEC))
        .tapError(exception => ZManaged.succeed(log.error(s"Server could not start with parameters [host:port]=[${interface},${port}]", exception)))
        .tap(future =>
          ZManaged.succeed {
            if (Desktop.isDesktopSupported) {
              Desktop.getDesktop
                .browse(new URI("[SAML_URL]"))
            }
            future.map(serverBinding => log.info(s"Server online at http:/${serverBinding.localAddress}"))(runtime.platform.executor.asEC)
          }
        )
    }
}
