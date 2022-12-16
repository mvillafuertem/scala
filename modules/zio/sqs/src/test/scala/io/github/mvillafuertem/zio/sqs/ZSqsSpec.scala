package io.github.mvillafuertem.zio.sqs

import io.github.mvillafuertem.zio.sqs.configuration.{ LocalstackConfiguration, SqsServiceConfiguration }
import zio._
import zio.aws.sqs.Sqs
import zio.aws.sqs.model.Message
import zio.sqs.producer.{ Producer, ProducerEvent }
import zio.sqs.serialization.Serializer
import zio.sqs.{ SqsStream, SqsStreamSettings, Utils }
import zio.test.Assertion.equalTo
import zio.test.TestAspect.beforeAll
import zio.test.{ assertZIO, Live, Spec, TestClock, TestEnvironment, ZIOSpecDefault }

object ZSqsSpec extends ZIOSpecDefault with LocalstackConfiguration with SqsServiceConfiguration {

  val test: Spec[Sqs with Live, Throwable] = test("send and receive messages")(
    assertZIO(
      sendAndGet(Seq("hola", "adios")).map(_.map(_.body.getOrElse("error")))
    )(equalTo(Chunk("hola", "adios")))
  )

  def withFastClock: ZIO[Live, Nothing, Long] =
    Live.withLive[Live, Nothing, Nothing, Unit, Long](TestClock.adjust(1.seconds))(_.repeat[Live, Long](Schedule.spaced(10.millis)))

  def sendAndGet(
    messages: Seq[String],
    settings: SqsStreamSettings = SqsStreamSettings(stopWhenQueueEmpty = true)
  ): ZIO[Sqs with Live, Throwable, Chunk[Message.ReadOnly]] =
    for {
      _                 <- withFastClock.fork
      _                 <- Utils
                             .createQueue("queue1")
                             .foldCause(_ => false, _ => true)
                             .debug("queue created")
      queueUrl          <- Utils.getQueueUrl("queue1")
      producer           = Producer.make(queueUrl, Serializer.serializeString)
      _                 <- ZIO.scoped(producer.flatMap(p => ZIO.foreach(messages)(it => p.produce(ProducerEvent(it)))))
      messagesFromQueue <- SqsStream(queueUrl, settings).runCollect
    } yield messagesFromQueue

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite(getClass.getSimpleName)(test).provideCustomLayerShared(sqsLayer) @@
      beforeAll(
        ZIO.acquireRelease(ZIO.attemptBlockingIO(container.start()))(_ => ZIO.succeedBlocking(container.close()))
      )

}
