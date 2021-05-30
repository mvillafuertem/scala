#!/usr/bin/env amm

import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`com.jsoniter:jsoniter:0.9.23`
import $ivy.`dev.zio::zio-sqs:0.4.2`
import $ivy.`dev.zio::zio-streams:1.0.8`
import $ivy.`dev.zio::zio:1.0.8`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import $ivy.`software.amazon.awssdk:aws-sdk-java:2.16.74`

import org.slf4j.{ Logger, LoggerFactory }
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{ PublishRequest, PublishResponse }
import zio.clock._
import zio.console._
import zio.{ Promise, Queue, Task, UIO, ZIO, _ }
import zio.stream.{ Stream, ZSink, ZStream }

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// amm `pwd`/SQSConsumer.sc
@main
def main(@arg(doc = "access key") key: String ,
         @arg(doc = "access secret") secret: String ,
         @arg(doc = "region") region: String ,
         @arg(doc = "name of queue") queue: String): Unit =
  SNSProducer.main(Array(key, secret, region, queue))

case class SNSProducerProperties(key: String, secret: String, region: String, queue: String)
type ZSNSProducerProperties = Has[SNSProducerProperties]

object SNSProducer extends zio.App {

  private val log = LoggerFactory.getLogger(getClass)

  override def run(args: List[String]) =
    program
      .provideCustomLayer(ZLayer.succeed(SNSProducerProperties(args.head, args(1), args(2), args(3))))
      .fold(_ => 1, _ => 0)

  def sendStream: Stream[Throwable, PublishRequest] => ZStream[ZSNSProducerProperties, Throwable, PublishResponse] =
    stream => {
      val value = make()
      stream.mapMPar(10)(p => value.use(snsAsyncClient => produce(snsAsyncClient)(p)))
    }

  private def produce(snsAsyncClient: SnsAsyncClient)(publishRequest: PublishRequest): RIO[ZSNSProducerProperties, PublishResponse] =
    Task.effectAsync[PublishResponse] { cb =>
      snsAsyncClient
        .publish(publishRequest)
        .handle[Unit] { (result, err) =>
          err match {
            case null => cb(IO.succeed(result))
            case ex   => cb(IO.fail(ex))
          }
        }
      ()
    }

  private def make(): ZManaged[ZSNSProducerProperties, Throwable, SnsAsyncClient] =
    ZManaged.fromEffect(
      for {
        credential <- credentialsProvider
        client     <- clientEffect(credential)
      } yield client
    )

  val program: ZIO[Console with Clock with ZSNSProducerProperties, Throwable, Unit] =
    (for {
      queue  <- ZIO.access[ZSNSProducerProperties](_.get.queue)
      request = List
                  .fill(10)("Hello World")
                  .map(msg =>
                    PublishRequest
                      .builder()
                      .targetArn(queue)
                      .message(msg)
                      .build()
                  )
      result <- sendStream(zio.stream.Stream(request: _*))
                  .map(_.toString)
                  .tap(r => UIO(log.info(s"List of results ${r}")))
                  .run(ZSink.drain)
      _      <- UIO(log.info(s"List of results ${result}"))
    } yield ())
      .tapError(e => UIO(log.error(s"$e")))

  private lazy val credentialsProvider =
    for {
      properties <- ZIO.access[ZSNSProducerProperties](_.get)
      credential <- Task(
                      StaticCredentialsProvider
                        .create(
                          AwsBasicCredentials
                            .create(properties.key, properties.secret)
                        )
                    )
    } yield credential

  private def clientEffect(credentialsProvider: StaticCredentialsProvider): ZIO[ZSNSProducerProperties, Throwable, SnsAsyncClient] =
    for {
      region <- ZIO.access[ZSNSProducerProperties](_.get.region)
      client <- Task(
                  SnsAsyncClient
                    .builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .build()
                )
    } yield client

}
