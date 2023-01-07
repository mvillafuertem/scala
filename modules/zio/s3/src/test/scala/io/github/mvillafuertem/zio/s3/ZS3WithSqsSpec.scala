package io.github.mvillafuertem.zio.s3

import io.github.mvillafuertem.zio.s3.configuration.{ LocalstackConfiguration, S3ServiceConfiguration, SqsServiceConfiguration }
import software.amazon.awssdk.services.s3.model.S3Exception
import zio.aws.sqs.Sqs
import zio.aws.sqs.model.Message
import zio.s3._
import zio.sqs.producer.{ Producer, ProducerEvent }
import zio.sqs.serialization.Serializer
import zio.sqs.{ SqsStream, SqsStreamSettings, Utils }
import zio.stream.{ ZPipeline, ZSink, ZStream }
import zio.test.Assertion.equalTo
import zio.test.TestAspect.beforeAll
import zio.test.{ assertZIO, Live, Spec, TestClock, TestEnvironment, ZIOSpecDefault }
import zio.{ durationInt, Chunk, Schedule, Scope, ZIO }

object ZS3WithSqsSpec extends ZIOSpecDefault with LocalstackConfiguration with S3ServiceConfiguration with SqsServiceConfiguration {

  private val test: Spec[S3 with Sqs with Live, Throwable] = test("consume messages from sqs and put object")(
    assertZIO(for {
      _                 <- createBucket(bucketName)
                             .foldCause(_ => false, _ => true)
                             .debug("bucket created")
      _                 <- Utils
                             .createQueue("queue1")
                             .foldCause(_ => false, _ => true)
                             .debug("queue created")
      queueUrl          <- Utils
                             .getQueueUrl("queue1")
                             .debug("queueUrl")
      _                 <- sendAndGet(Seq("message1"), queueUrl)
                             .debug("sqs")
      contentBroadcast0 <- getObjectFrom("broadcast0")
      contentBroadcast1 <- getObjectFrom("broadcast1")
    } yield contentBroadcast0 ++ contentBroadcast1)(equalTo(Chunk("message1", "message1")))
  )

  private def getObjectFrom(key: String): ZIO[S3, Exception, Chunk[String]] =
    getObject(bucketName, key)
      .via(ZPipeline.utf8Decode)
      .filter(_.nonEmpty)
      .runCollect

  def withFastClock: ZIO[Live, Nothing, Long] =
    Live.withLive[Live, Nothing, Nothing, Unit, Long](TestClock.adjust(1.seconds))(_.repeat[Live, Long](Schedule.spaced(10.millis)))

  def messageToObject(message: Message.ReadOnly, key: String): ZIO[S3 with Any, S3Exception, Unit] = {
    val bytes = message.body
      .fold("error")(identity)
      .getBytes
    val chunk = Chunk.fromArray(bytes)
    val data  = ZStream.fromChunks(chunk)
    putObject(bucketName, key, chunk.length.toLong, data)
  }

  def sendAndGet(
    messages: Seq[String],
    queueUrl: String,
    settings: SqsStreamSettings = SqsStreamSettings(stopWhenQueueEmpty = true)
  ): ZIO[S3 with Sqs with Live, Throwable, Unit] =
    for {
      _                 <- withFastClock.fork
      producer           = Producer.make(queueUrl, Serializer.serializeString)
      _                 <- ZIO.scoped(
                             producer.flatMap(p =>
                               ZIO
                                 .foreach(messages)(it => p.produce(ProducerEvent(it)))
                                 .debug("producer")
                             )
                           )
      messagesFromQueue <- ZIO.scoped(
                             SqsStream(queueUrl, settings)
                               .tap(a => ZIO.debug(s"consumer: ${a.body}"))
                               .broadcast(2, 5)
                               .flatMap { broadcast =>
                                 for {
                                   out1 <- broadcast(0)
                                             .mapZIO(msg => messageToObject(message = msg, key = "broadcast0"))
                                             .runDrain
                                             .fork
                                   out2 <- broadcast(1)
                                             .mapZIO(msg => messageToObject(message = msg, key = "broadcast1"))
                                             .run(ZSink.drain)
                                             .fork
                                   _    <- out1.join.zipPar(out2.join)
                                 } yield ()
                               }
                           )
    } yield messagesFromQueue

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite(getClass.getSimpleName)(test).provideCustomLayerShared(sqsLayer >+> s3Layer) @@
      beforeAll(
        ZIO.acquireRelease(ZIO.attemptBlockingIO(container.start()))(_ => ZIO.succeedBlocking(container.close()))
      )

}
