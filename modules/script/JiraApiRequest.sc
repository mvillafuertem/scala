#!/usr/bin/env amm

import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`com.softwaremill.sttp.client::akka-http-backend:2.2.9`
import $ivy.`com.softwaremill.sttp.client::async-http-client-backend-zio:2.2.9`
import $ivy.`com.softwaremill.sttp.client::circe:2.2.9`
import $ivy.`com.softwaremill.sttp.client::core:2.2.9`
import $ivy.`dev.zio::zio:1.0.7`
import $ivy.`io.circe::circe-generic-extras:0.13.0`
import $ivy.`io.circe::circe-generic:0.13.0`
import $ivy.`io.circe::circe-optics:0.13.0`
import $ivy.`io.circe::circe-parser:0.13.0`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import io.circe.optics.JsonPath._
import io.circe.parser._
import org.slf4j.{ Logger, LoggerFactory }
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.{ basicRequest, Request, _ }
import zio.console.putStrLn
import zio.duration.durationInt
import zio.{ ExitCode, IO, Schedule, Task, URIO }

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// amm `pwd`/app/modules/script/JiraApiRequest.sc
@main
def main(): Unit = JiraApiRequest.main(Array())

// https://docs.atlassian.com/software/jira/docs/api/REST/7.6.1
// https://developer.atlassian.com/server/jira/platform/jira-rest-api-examples/#getting-metadata-for-creating-issues-examples
// SELECT ps.ID, pe.PROPERTY_KEY, ps.PROPERTYVALUE
// FROM PROPERTYENTRY pe, PROPERTYSTRING ps
// WHERE pe.id = ps.id AND pe.PROPERTY_KEY LIKE 'build';
object JiraApiRequest extends zio.App {

  private val log = LoggerFactory.getLogger(getClass)

  val backend = AsyncHttpClientZioBackend()

  private val uri = "http://192.168.0.21:2990/jira/rest"

  private val authPOST = basicRequest
    .post(uri"$uri/auth/1/session")
    .contentType("application/json")
    .body("""
            |{
            |    "username": "admin",
            |    "password": "admin"
            |}
            |""".stripMargin)

  private def issueGET(issue: String): Request[Either[String, String], Nothing] = basicRequest
    .get(uri"$uri/api/2/issue/$issue")

  private def issuePOST(issue: String): Request[Either[String, String], Nothing] = basicRequest
    .post(uri"$uri/api/2/issue")

  private val metadataGETFilterByProjectKey: Request[Either[String, String], Nothing] = basicRequest
    .get(uri"$uri/api/2/issue/createmeta?projectKeys=TL")

  private val metadataGETFilterByProjectKeyAndDiscoverIssueFieldData: Request[Either[String, String], Nothing] = basicRequest
    .get(uri"$uri/api/2/issue/createmeta?projectKeys=TL&issuetypeNames=Feature&expand=projects.issuetypes.fields")

  private val metadataGETDiscoverIssueFieldData: Request[Either[String, String], Nothing] = basicRequest
    .get(uri"$uri/api/2/issue/createmeta/TL/issuetypes/10000")

  val _modifyCustomField = root.fields.customfield_13500.each.string.modify(_ => "HOLA MUNDO")

  def schedule = Schedule.recurs(4) && Schedule.spaced(1.second)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    backend.flatMap { implicit backend =>
      for {
        authResponse     <- authPOST.send()
        issue             = "TL-2097"
        issueGETResponse <- issueGET(issue).cookies(authResponse).send()
        body             <- IO.fromEither(issueGETResponse.body).mapError(new RuntimeException(_))
        jsonBody         <- Task.fromEither(parse(body).map(_modifyCustomField).map(_.noSpaces))
        metadata         <- metadataGETDiscoverIssueFieldData.cookies(authResponse).send()
        _                <- putStrLn("_________________")
        _                <- putStrLn(metadata.body.getOrElse("💣 ERROR 💥"))
        _                <- putStrLn("_________________")
        issueResponse    <- issuePOST(issue)
                              .contentType("application/json")
                              .body("""
                                   |{
                                   |    "fields": {
                                   |       "project":
                                   |       {
                                   |          "key": "TL"
                                   |       },
                                   |       "summary": "REST ye merry gentlemen.",
                                   |       "description": "Creating of an issue using project keys and issue type names using the REST API",
                                   |       "issuetype": {
                                   |          "name": "Feature"
                                   |       },
                                   |       "labels": ["PR"],
                                   |       "customfield_13500" : ["TO REPRODUCE ERROR"],
                                   |       "customfield_10006" : "aDa ZIO Rest API",
                                   |       "customfield_13300" : ["1592201"],
                                   |       "customfield_10264" : ["P1"]
                                   |   }
                                   |}
                                   |""".stripMargin)
                              .cookies(authResponse)
                              .send()
                              .repeatN(100)

      } yield issueResponse
    }.fold(
      { e =>
        log.error(e.getMessage, e)
        ExitCode.failure
      },
      { r =>
        log.info(s"Status Code ~> ${r.code}")
        r.body match {
          case Left(_)      => log.error(r.toString())
          case Right(value) => log.info(value)
        }
        ExitCode.success
      }
    )

}
