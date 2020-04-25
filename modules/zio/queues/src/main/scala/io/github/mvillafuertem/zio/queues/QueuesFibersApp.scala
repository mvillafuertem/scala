package io.github.mvillafuertem.zio.queues

import io.github.mvillafuertem.zio.queues.consumer.{ Consumer, ConsumerSettings }
import io.github.mvillafuertem.zio.queues.consumer.{ Consumer, ConsumerSettings }
import io.github.mvillafuertem.zio.queues.model.OrderGenerator.IntRequestGenerator
import io.github.mvillafuertem.zio.queues.producer.{ Producer, ProducerSettings }
import io.github.mvillafuertem.zio.queues.infrastructure.Exchange
import io.github.mvillafuertem.zio.queues.producer.{ Producer, ProducerSettings }
import zio._

object QueuesFibersApp extends App {

  override def run(args: List[String]) = program.map(_ => 0)

  val program = for {

    exchange                                   <- Exchange.createM[Int]
    ctxExchange                                <- exchange.run
    (listenerTopic, kitchenTopic, barTopic, _) = ctxExchange

    generator = IntRequestGenerator()
    xRayRoom  <- Producer.make[Int](ProducerSettings("Waiter", listenerTopic))
    _         <- xRayRoom.produce(generator)

    chef    <- Consumer.make[Int](ConsumerSettings("Chef", 10, scala.Console.GREEN))
    barman1 <- Consumer.make[Int](ConsumerSettings("Barman 1", 10, scala.Console.BLUE))
    barman2 <- Consumer.make[Int](ConsumerSettings("Barman 2", 10, scala.Console.MAGENTA))
    sales   <- Consumer.make[Int](ConsumerSettings("Sales", 10, scala.Console.RED))

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

}
