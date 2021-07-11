import $ivy.`ch.qos.logback:logback-classic:1.2.3`
import $ivy.`com.jsoniter:jsoniter:0.9.23`
import $ivy.`dev.zio::zio-sqs:0.2.2`
import $ivy.`dev.zio::zio-streams:1.0.9`
import $ivy.`dev.zio::zio:1.0.9`
import $ivy.`org.slf4j:slf4j-api:1.7.30`
import $ivy.`software.amazon.awssdk:aws-sdk-java:2.16.74`

import org.slf4j.{ Logger, LoggerFactory }
import zio.clock.Clock
import zio.console.Console
import zio.{ Has, UIO, ZIO, ZLayer }

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
rootLogger.setLevel(ch.qos.logback.classic.Level.INFO)

// amm `pwd`/SQSConsumer.sc
@main
def main(@arg(doc = "access key") key: String ,
         @arg(doc = "access secret") secret: String ,
         @arg(doc = "region") region: String ,
         @arg(doc = "name of queue") queue: String): Unit =
  AWS.main(Array(key, secret, region, queue))

case class AWSProperties(key: String, secret: String, region: String, queue: String)
type ZAWSProperties = Has[AWSProperties]

object AWS extends zio.App {

  private val log = LoggerFactory.getLogger(getClass)

  def run(args: List[String]) =
    myAppLogic
      .provideCustomLayer(ZLayer.succeed(AWSProperties(args.head, args(1), args(2), args(3))))
      .fold(_ => 1, _ => 0)

  val myAppLogic: ZIO[Console with Clock with ZAWSProperties, Throwable, Unit] =
    (for {
      properties <- ZIO.access[ZAWSProperties](_.get)
      _          <- UIO(log.info(s"Ready to receive messages from ${properties}"))
    } yield ())
  //.tapError(_ => UIO(log.error(s"e")))
}
