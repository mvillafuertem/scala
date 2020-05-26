package io.github.mvillafuertem.zio.queues

import io.github.mvillafuertem.zio.queues.consumer.{Consumer, ConsumerRecord, ConsumerSettings}
import io.github.mvillafuertem.zio.queues.infrastructure.Topic
import io.github.mvillafuertem.zio.queues.producer.{Producer, ProducerRecord, ProducerSettings}
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

object RestaurantOrderExchangeApp extends zio.App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    program.exitCode

  val program: ZIO[Console with Clock with Random, Nothing, Unit] = for {

    exchange                                  <- Exchange.createM
    ctxExchange                               <- exchange.run
    (listenerTopic, kitchenTopic, barTopic, _) = ctxExchange

    producer <- Producer.make[Order](ProducerSettings("Waiter", listenerTopic))
    _        <- producer.produce(ProducerRecord[Order](1, Bacon))

    chef    <- Consumer.make[Order](ConsumerSettings("Chef", 10, scala.Console.GREEN))
    barman1 <- Consumer.make[Order](ConsumerSettings("Barman 1", 10, scala.Console.BLUE))
    barman2 <- Consumer.make[Order](ConsumerSettings("Barman 2", 10, scala.Console.MAGENTA))
    sales   <- Consumer.make[Order](ConsumerSettings("Sales", 10, scala.Console.RED))

    chefContext    <- chef.consume()
    barman1Context <- barman1.consume()
    barman2Context <- barman2.consume()
    salesContext   <- sales.consume()

    (chefQueue, chefFiber) = chefContext
    (barman1Queue, _)      = barman1Context
    (barman2Queue, _)      = barman2Context
    (salesQueue, _)        = salesContext

    outputQueueHip  <- kitchenTopic.subscribeM(chefQueue, 1)
    outputQueueKnee <- barTopic.subscribeM(barman1Queue, 1)
    outputQueueKnee <- outputQueueKnee.subscribeM(barman2Queue, 1)
    outputQueueHip  <- outputQueueHip.subscribeM(salesQueue, 2)
    outputQueueKnee <- outputQueueKnee.subscribeM(salesQueue, 2)

    _ <- outputQueueHip.run
    _ <- outputQueueKnee.run

    _ <- chefFiber.join

  } yield ()

  sealed trait Order

  case object Coffee extends Order

  case object Sandwich extends Order

  case object Bacon extends Order

  trait Exchange {

    val jobQueueM: UIO[Queue[ProducerRecord[Order]]] = Queue.bounded[ProducerRecord[Order]](10)
    val kitchenM: UIO[Queue[ConsumerRecord[Order]]]  = Queue.bounded[ConsumerRecord[Order]](10)
    val barM: UIO[Queue[ConsumerRecord[Order]]]      = Queue.bounded[ConsumerRecord[Order]](10)

    def run: UIO[(Queue[ProducerRecord[Order]], Topic[Order], Topic[Order], Fiber.Runtime[Nothing, Nothing])] =
      for {
        jobQueue    <- jobQueueM
        kitchen     <- kitchenM
        bar         <- barM
        kitchenTopic = Topic.Live(kitchen, Map.empty)
        barTopic     = Topic.Live(bar, Map.empty)
        loop         = for {
                 job <- jobQueue.take
                 _   <- job.value match {
                        case Coffee   => bar.offer(ConsumerRecord(job.id, job.value))
                        case Sandwich => kitchen.offer(ConsumerRecord(job.id, job.value))
                        case Bacon    => kitchen.offer(ConsumerRecord(job.id, job.value))
                      }
               } yield ()
        fiber       <- loop.forever.fork
      } yield (jobQueue, kitchenTopic, barTopic, fiber)
  }

  object Exchange {
    def createM: UIO[Exchange] = ZIO.succeed(new Exchange {})
  }

}
