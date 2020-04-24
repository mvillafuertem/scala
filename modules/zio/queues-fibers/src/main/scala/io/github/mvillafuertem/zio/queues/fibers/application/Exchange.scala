package io.github.mvillafuertem.zio.queues.fibers.application

import io.github.mvillafuertem.zio.queues.fibers.model.Diagnostic.{ HipDiagnostic, KneeDiagnostic }
import io.github.mvillafuertem.zio.queues.fibers.model.RequestGenerator.Request
import zio.{ Queue, ZIO }

case class Exchange[A]() {
  val queueHipM  = Queue.bounded[A](10)
  val queueKneeM = Queue.bounded[A](10)
  val jobQueueM  = Queue.bounded[Request[A]](10)

  def run =
    for {
      jobQueue       <- jobQueueM
      queueHip       <- queueHipM
      queueKnee      <- queueKneeM
      hipTopicQueue  = TopicQueue(queueHip, Map.empty)
      kneeTopicQueue = TopicQueue(queueKnee, Map.empty)
      loop = for {
        job <- jobQueue.take
        _ <- job.topic match {
              case HipDiagnostic =>
                queueHip.offer(job.XRayImage)
              case KneeDiagnostic =>
                queueKnee.offer(job.XRayImage)
            }
      } yield ()
      fiber <- loop.forever.fork
    } yield (jobQueue, hipTopicQueue, kneeTopicQueue, fiber)
}

object Exchange {
  def createM[A] = ZIO.succeed(Exchange[A]())
}
