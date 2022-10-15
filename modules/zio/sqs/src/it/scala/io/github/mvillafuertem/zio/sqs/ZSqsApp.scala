package io.github.mvillafuertem.zio.sqs

import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import zio.Console.printLine
import zio.aws.core.config.CommonAwsConfig
import zio.{ durationInt, Schedule, Scope, ZIO, ZLayer }
import zio.aws.sqs.Sqs
import zio.aws.sqs.model.CreateQueueRequest
import zio.sqs.{ SqsStream, SqsStreamSettings }
import zio.test.Assertion.equalTo
import zio.test.{ assertZIO, Spec, TestEnvironment, ZIOSpecDefault }

import java.net.URI

object ZSqsApp extends ZIOSpecDefault {

//  val client: ZLayer[Scope, Throwable, Sqs] = zio.aws.netty.NettyHttpClient.default ++
//    ZLayer.succeed(
//      CommonAwsConfig(
//        region = Some(Region.US_EAST_1),
//        credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretAccessKey)),
//        endpointOverride = Some(URI.create("http://localhost:4566")),
//        commonClientConfig = None
//      )
//    ) >>>
//    zio.aws.core.config.AwsConfig.configured() >>>
//    zio.aws.sqs.Sqs.live
//
//  val value: Spec[Any, Object] = test("wip")(
//    assertZIO(
//      effect = for {
//        queueUrl <- Sqs
//                      .createQueue(CreateQueueRequest("queue1"))
//                      .flatMap(_.getQueueUrl)
//                      .debug("asdf")
//        queue    <- SqsStream(queueUrl, SqsStreamSettings(stopWhenQueueEmpty = true))
//                      .map(_.body)
//                      .map(_.getOrElse(""))
//                      .broadcast(2, 5)
//                      .flatMap { streams =>
//                        for {
//                          out1 <- streams(0)
//                                    .mapZIO(x => printLine(s"Maximum: $x"))
//                                    .runDrain
//                                    .fork
//                          out2 <- streams(1)
//                                    .schedule(Schedule.spaced(1 second))
//                                    .foreach(x => printLine(s"Logging to the Console: $x"))
//                                    .fork
//                          _    <- out1.join.zipPar(out2.join)
//                        } yield ()
//                      }
//      } yield queue
//    )(equalTo(()))
//  ).provideLayer(client)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite(getClass.getSimpleName)(
    test("wip")(assertZIO(ZIO.succeed("asdf"))(equalTo("asdf")))
  )

}
