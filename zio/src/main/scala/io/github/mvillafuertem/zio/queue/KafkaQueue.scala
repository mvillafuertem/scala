package io.github.mvillafuertem.zio.queue

import zio._
import zio.console._
import zio.duration.Duration
import scala.util.Random
import java.util.concurrent.TimeUnit

object Main extends App {

  override def run(args: List[String]) = program.fold(_ => 1, _ => 0)

  sealed trait Diagnostic

  case object HipDiagnostic extends Diagnostic

  case object KneeDiagnostic extends Diagnostic

  case class Request[A](topic: Diagnostic, XRayImage: A)

  trait RequestGenerator[A] {
    def generate(topic: Diagnostic): Request[A]
  }

  case class IntRequestGenerator() extends RequestGenerator[Int] {
    override def generate(topic: Diagnostic): Request[Int] = Request(topic, Random.nextInt(1000))
  }

  case class Consumer[A](title: String) {
    val queueM = Queue.bounded[A](10)

    def run = for {
      queue <- queueM
      loop = for {
        img <- queue.take
        _ <- putStrLn(s"[$title] worker: Starting analyzing task $img")
        _ <- ZIO.sleep(Duration(Random.nextInt(4), TimeUnit.SECONDS))
        _ <- putStrLn(s"[$title] worker: Finished task $img")
      } yield ()
      fiber <- loop.forever.fork
    } yield (queue, fiber)
  }

  object Consumer {
    def createM[A](title: String) = UIO.succeed(Consumer[A](title))
  }

  case class TopicQueue[A](queue: Queue[A], subscribers: Map[Int, List[Queue[A]]]) {
    def subscribeM(sub: Queue[A], consumerGroup: Int): UIO[TopicQueue[A]] = {
      val updatedMap = subscribers.get(consumerGroup) match {
        case Some(value) =>
          subscribers + (consumerGroup -> (value :+ sub))
        case None =>
          subscribers + (consumerGroup -> List(sub))
      }

      UIO.succeed(copy(subscribers = updatedMap))
    }

    def run = {
      def randomElement(list: List[Queue[A]]) = if (list.nonEmpty) {
        Some(list(Random.nextInt(list.length)))
      } else {
        None
      }
      val loop = for {
        elem <- queue.take
        mapped = subscribers.values.flatMap(randomElement(_).map(_.offer(elem)))
        _ <- ZIO.collectAll(mapped)
      } yield ()
      loop.forever.fork
    }

  }

  object TopicQueue {
    def createM[A](queue: Queue[A]): UIO[TopicQueue[A]] = UIO.succeed(TopicQueue(queue, Map.empty))
  }

  case class Exchange[A]() {
    val queueHipM = Queue.bounded[A](10)
    val queueKneeM = Queue.bounded[A](10)
    val jobQueueM = Queue.bounded[Request[A]](10)

    def run = for {
      jobQueue <- jobQueueM
      queueHip <- queueHipM
      queueKnee <- queueKneeM
      hipTopicQueue = TopicQueue(queueHip, Map.empty)
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

  case class Producer[A](queue: Queue[Request[A]], generator: RequestGenerator[A]) {
    def run = {
      val loop = for {
        _ <- putStrLn("[XRayRoom] generating hip and knee request")
        _ <- queue.offer(generator.generate(HipDiagnostic))
        _ <- queue.offer(generator.generate(KneeDiagnostic))
        _ <- ZIO.sleep(Duration(2, TimeUnit.SECONDS))
      } yield ()
      loop.forever.fork
    }
  }

  object Producer {
    def createM[A](queue: Queue[Request[A]], generator: RequestGenerator[A]) = UIO.succeed(Producer(queue, generator))
  }

  val program = for {

    physicianHip <- Consumer.createM[Int]("Hip")
    ctxPhHip <- physicianHip.run
    (phHipQueue, phHipFiber) = ctxPhHip

    loggerHip <- Consumer.createM[Int]("HIP_LOGGER")
    ctxLoggerHip <- loggerHip.run
    (loggerHipQueue, _) = ctxLoggerHip

    physicianKnee <- Consumer.createM[Int]("Knee1")
    ctxPhKnee <- physicianKnee.run
    (phKneeQueue, _) = ctxPhKnee

    physicianKnee2 <- Consumer.createM[Int]("Knee2")
    ctxPhKnee2 <- physicianKnee2.run
    (phKneeQueue2, _) = ctxPhKnee2


    exchange <- Exchange.createM[Int]
    ctxExchange <- exchange.run
    (inputQueue, outputQueueHip, outputQueueKnee, _) = ctxExchange


    generator = IntRequestGenerator()
    xRayRoom <- Producer.createM[Int](inputQueue, generator)
    _ <- xRayRoom.run


    outputQueueHip <- outputQueueHip.subscribeM(phHipQueue, consumerGroup = 1)
    outputQueueHip <- outputQueueHip.subscribeM(loggerHipQueue, consumerGroup = 2)

    outputQueueKnee <- outputQueueKnee.subscribeM(phKneeQueue, consumerGroup = 1)
    outputQueueKnee <- outputQueueKnee.subscribeM(phKneeQueue2, consumerGroup = 1)

    _ <- outputQueueHip.run
    _ <- outputQueueKnee.run

    _ <- phHipFiber.join

  } yield ()

}
