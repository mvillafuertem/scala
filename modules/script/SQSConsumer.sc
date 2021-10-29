#!/usr/bin/env amm

import $ivy.`ch.qos.logback:logback-classic:1.2.4`
import $ivy.`com.jsoniter:jsoniter:0.9.23`
import $ivy.`dev.zio::zio-sqs:0.4.2`
import $ivy.`dev.zio::zio-streams:1.0.10`
import $ivy.`dev.zio::zio:1.0.10`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import $ivy.`software.amazon.awssdk:aws-sdk-java:2.16.74`

import java.io.{ BufferedOutputStream, File, FileOutputStream }
import com.jsoniter.output.JsonStream
import zio.clock.Clock
import org.slf4j.{ Logger, LoggerFactory }
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message
import zio.console._
import zio.duration._
import zio.sqs.{ SqsStream, Utils }
import zio.{ Task, UIO, ZIO, _ }

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// amm `pwd`/SQSConsumer.sc
@main
def main(
  @arg(doc = "access key") key: String,
  @arg(doc = "access secret") secret: String,
  @arg(doc = "region") region: String,
  @arg(doc = "name of queue") queue: String
): Unit =
  SQSConsumer.main(Array(key, secret, region, queue))

case class SQSConsumerProperties(key: String, secret: String, region: String, queue: String)
type ZSQSConsumerProperties = Has[SQSConsumerProperties]

object SQSConsumer extends zio.App {

  private val log = LoggerFactory.getLogger(getClass)

  def run(args: List[String]) =
    program
      .provideCustomLayer(ZLayer.succeed(SQSConsumerProperties(args.head, args(1), args(2), args(3))))
      .exitCode

  val program: ZIO[Console with Clock with ZSQSConsumerProperties, Throwable, Unit] =
    (for {
      queue      <- ZIO.access[ZSQSConsumerProperties](_.get.queue)
      credential <- credentialsProvider
      client     <- clientEffect(credential)
      queueUrl   <- Utils.getQueueUrl(client, queue)
      consumer   <- consumer(client, queueUrl).forever.fork
      _          <- UIO(log.info(s"Ready to receive messages from ${queueUrl}"))
      _          <- consumer.join
    } yield ())
      .tapError(e => UIO(log.error(s"$e")))

  private lazy val credentialsProvider =
    for {
      properties <- ZIO.access[ZSQSConsumerProperties](_.get)
      credential <- Task(
                      StaticCredentialsProvider
                        .create(
                          AwsBasicCredentials
                            .create(properties.key, properties.secret)
                        )
                    )
    } yield credential

  private def clientEffect(credentialsProvider: StaticCredentialsProvider) =
    for {
      region <- ZIO.access[ZSQSConsumerProperties](_.get.region)
      client <- Task(
                  SqsAsyncClient
                    .builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .build()
                )
    } yield client

  private def consumer(client: SqsAsyncClient, queueUrl: String) =
    SqsStream(client, queueUrl)
      .schedule(Schedule.spaced(1.second))
      .tap(writeToFile)
      .runCollect

  private def writeToFile(message: Message) =
    for {
      file <- Task.effect(new File("/tmp/sqsconsumer.json"))
      json <- Task.effect(JsonStream.serialize(message)).tap(msg => putStrLn(msg))
      _    <-
        Task(new BufferedOutputStream(new FileOutputStream(file, true))).bracket(os => UIO(os.close()))(os => Task.effect(os.write(json.map(_.toByte).toArray)))
    } yield ()

}
