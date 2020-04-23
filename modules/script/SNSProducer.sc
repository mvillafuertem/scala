#!/usr/bin/env amm

import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`com.jsoniter:jsoniter:0.9.23`
import $ivy.`dev.zio::zio-sqs:0.2.2`
import $ivy.`dev.zio::zio-streams:1.0.0-RC18-2`
import $ivy.`dev.zio::zio:1.0.0-RC18-2`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import $ivy.`software.amazon.awssdk:annotations:2.13.1`
import $ivy.`software.amazon.awssdk:apache-client:2.13.1`
import $ivy.`software.amazon.awssdk:auth:2.13.1`
import $ivy.`software.amazon.awssdk:aws-core:2.13.1`
import $ivy.`software.amazon.awssdk:aws-query-protocol:2.13.1`
import $ivy.`software.amazon.awssdk:http-client-spi:2.13.1`
import $ivy.`software.amazon.awssdk:netty-nio-client:2.13.1`
import $ivy.`software.amazon.awssdk:profiles:2.13.1`
import $ivy.`software.amazon.awssdk:protocol-core:2.13.1`
import $ivy.`software.amazon.awssdk:regions:2.13.1`
import $ivy.`software.amazon.awssdk:sdk-core:2.13.1`
import $ivy.`software.amazon.awssdk:sns:2.13.1`
import $ivy.`software.amazon.awssdk:utils:2.13.1`
import org.slf4j.{Logger, LoggerFactory}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{PublishRequest, PublishResponse}
import zio.clock._
import zio.console._
import zio.{Task, UIO, ZIO, _}

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// amm `pwd`/SQSConsumer.sc
@main
def main(key: String@doc("access key"),
         secret: String@doc("access secret"),
         region: String@doc("region"),
         queue: String@doc("name of queue")): Unit = {

  SNSProducer.main(Array(key, secret, region, queue))
}


case class SNSProducerProperties(key: String, secret: String, region: String, queue: String)
type ZSNSProducerProperties = Has[SNSProducerProperties]

object SNSProducer extends zio.App {

  private val log = LoggerFactory.getLogger(getClass)

  override def run(args: List[String]) =
    program.provideCustomLayer(
      ZLayer.succeed(
        SNSProducerProperties(args.head, args(1), args(2), args(3))))
      .fold(_ => 1, _ => 0)


  val program: ZIO[Console with Clock with ZSNSProducerProperties, Throwable, Unit] =
    (for {
      queue <- ZIO.access[ZSNSProducerProperties](_.get.queue)
      credential <- credentialsProvider
      client <- clientEffect(credential)
      response <- Task.effectAsync[PublishResponse] { cb =>
        client.publish(PublishRequest
          .builder()
          .targetArn("")
          .message("Hello World")
          .build())
          .handle[Unit] { (result, err) =>
          err match {
            case null => cb(IO.succeed(result))
            case ex   => cb(IO.fail(ex))
          }
        }
        ()
      }
      _ <- UIO(log.info(s"List of topics ${response}"))
    } yield ())
      .tapError(e => UIO(log.error(s"$e")))

  private lazy val credentialsProvider =
    for {
      properties <- ZIO.access[ZSNSProducerProperties](_.get)
      credential <- Task(StaticCredentialsProvider
        .create(AwsBasicCredentials
          .create(properties.key, properties.secret)))
    } yield credential


  private def clientEffect(credentialsProvider: StaticCredentialsProvider): ZIO[ZSNSProducerProperties, Throwable, SnsAsyncClient] =
    for {
      region <- ZIO.access[ZSNSProducerProperties](_.get.region)
      client <- Task(SnsAsyncClient
        .builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider)
        .build())
    } yield client


}