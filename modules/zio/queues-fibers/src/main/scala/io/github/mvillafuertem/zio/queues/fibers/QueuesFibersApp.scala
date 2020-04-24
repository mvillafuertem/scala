package io.github.mvillafuertem.zio.queues.fibers

import io.github.mvillafuertem.zio.queues.fibers.application.Consumer
import io.github.mvillafuertem.zio.queues.fibers.model.RequestGenerator.IntRequestGenerator
import zio._

object QueuesFibersApp extends App {

  override def run(args: List[String]) = program.map(_ => 0)

  val program = for {

    physicianHip             <- Consumer.createM[Int]("Hip")
    ctxPhHip                 <- physicianHip.run
    (phHipQueue, phHipFiber) = ctxPhHip

    loggerHip           <- Consumer.createM[Int]("HIP_LOGGER")
    ctxLoggerHip        <- loggerHip.run
    (loggerHipQueue, _) = ctxLoggerHip

    physicianKnee    <- Consumer.createM[Int]("Knee1")
    ctxPhKnee        <- physicianKnee.run
    (phKneeQueue, _) = ctxPhKnee

    physicianKnee2    <- Consumer.createM[Int]("Knee2")
    ctxPhKnee2        <- physicianKnee2.run
    (phKneeQueue2, _) = ctxPhKnee2

    exchange                                         <- Exchange.createM[Int]
    ctxExchange                                      <- exchange.run
    (inputQueue, outputQueueHip, outputQueueKnee, _) = ctxExchange

    generator = IntRequestGenerator()
    xRayRoom  <- Producer.createM[Int](inputQueue, generator)
    _         <- xRayRoom.run

    outputQueueHip <- outputQueueHip.subscribeM(phHipQueue, consumerGroup = 1)
    outputQueueHip <- outputQueueHip.subscribeM(loggerHipQueue, consumerGroup = 2)

    outputQueueKnee <- outputQueueKnee.subscribeM(phKneeQueue, consumerGroup = 1)
    outputQueueKnee <- outputQueueKnee.subscribeM(phKneeQueue2, consumerGroup = 1)

    _ <- outputQueueHip.run
    _ <- outputQueueKnee.run

    _ <- phHipFiber.join

  } yield ()

}
