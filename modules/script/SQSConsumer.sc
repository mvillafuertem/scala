#!/usr/bin/env amm

import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`com.jsoniter:jsoniter:0.9.23`
import $ivy.`dev.zio::zio-sqs:0.2.2`
import $ivy.`dev.zio::zio-streams:1.0.0-RC18-2`
import $ivy.`dev.zio::zio:1.0.0-RC18-2`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import $ivy.`software.amazon.awssdk:annotations:2.13.0`
import $ivy.`software.amazon.awssdk:apache-client:2.13.0`
import $ivy.`software.amazon.awssdk:auth:2.13.0`
import $ivy.`software.amazon.awssdk:aws-core:2.13.0`
import $ivy.`software.amazon.awssdk:aws-query-protocol:2.13.0`
import $ivy.`software.amazon.awssdk:http-client-spi:2.13.0`
import $ivy.`software.amazon.awssdk:netty-nio-client:2.13.0`
import $ivy.`software.amazon.awssdk:profiles:2.13.0`
import $ivy.`software.amazon.awssdk:protocol-core:2.13.0`
import $ivy.`software.amazon.awssdk:regions:2.13.0`
import $ivy.`software.amazon.awssdk:sdk-core:2.13.0`
import $ivy.`software.amazon.awssdk:sqs:2.13.0`
import $ivy.`software.amazon.awssdk:utils:2.13.0`
import com.jsoniter.output.JsonStream
import org.slf4j.{Logger, LoggerFactory}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import zio.clock._
import zio.console._
import zio.duration._
import zio.sqs.{SqsStream, SqsStreamSettings, Utils}
import zio.{Task, UIO, ZIO, _}

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.ERROR)

// amm `pwd`/SQSConsumer.sc
@main
def main(key: String @doc("access key"),
         secret: String @doc("access secret"),
         region: String @doc("region"),
         queue: String @doc("name of queue")): Unit =
  MyApp.main(Array(key, secret, region, queue))

object MyApp extends zio.App {

  private val log = LoggerFactory.getLogger(getClass)

  def run(args: List[String]) =
    myAppLogic(args.head, args(1), args(2), args(3))
      .fold(_ => 1, _ => 0)

  def myAppLogic(key: String,
                 secret: String,
                 region: String,
                 queue: String) =
    (for {
      credential <- credentialsProvider(key, secret)
      client <- clientEffect(credential, region)
      _ <- consumer(client, queue).forever
    } yield ())
      .tapError(e => UIO(log.error(s"$e")))


  private def credentialsProvider(key: String,
                                  secret: String) =
    Task(StaticCredentialsProvider
      .create(AwsBasicCredentials
        .create(key, secret)))


  private def clientEffect(credentialsProvider: StaticCredentialsProvider,
                           region: String): Task[SqsAsyncClient] =
    Task(SqsAsyncClient
      .builder()
      .region(Region.of(region))
      .credentialsProvider(credentialsProvider)
      .build())

  private def consumer(client: SqsAsyncClient, name: String): ZIO[Console with Clock, Throwable, Unit] =
    for {
      queueUrl <- Utils.getQueueUrl(client, name)
      _ <- SqsStream(client, queueUrl)
        .schedule(Schedule.spaced(1.second))
        .tap(msg => putStrLn(JsonStream.serialize(msg)))
        .runCollect
    } yield ()

}