package io.github.mvillafuertem.alpakka.kafka

import akka.NotUsed
import akka.kafka.ConsumerMessage.PartitionOffset
import akka.kafka.scaladsl.{Consumer, Transactional}
import akka.kafka.testkit.scaladsl.{ScalatestKafkaSpec, TestcontainersKafkaLike}
import akka.kafka.{ConsumerMessage, ProducerMessage, Subscriptions}
import akka.stream.Supervision.{Restart, Stop}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Zip}
import akka.stream.testkit.scaladsl.StreamTestKit.assertAllStagesStopped
import akka.stream.{ActorAttributes, FlowShape, KillSwitches}
import io.github.mvillafuertem.alpakka.kafka.NumbersKafkaIT.SpecBase
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.Ignore
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Ignore // sh: /testcontainers_start.sh: Permission denied
final class NumbersKafkaIT extends SpecBase with TestcontainersKafkaLike {

  implicit val patience = PatienceConfig(15.seconds, 1.second)

  it should "numbers" in assertAllStagesStopped {

    // G I V E N
    val sourceTopic = createTopic(1)
    val sinkTopic   = createTopic(2)
    val group       = createGroupId(1)

    Await.result(produce(sourceTopic, 1 to 10), remainingOrDefault)

    val consumerSettings = consumerDefaults().withGroupId(group)
    val producerSettings = producerDefaults
      .withParallelism(20)
      .withCloseTimeout(Duration.Zero)

    val numbersFlow = Flow[ConsumerMessage.TransactionalMessage[String, String]]
      .map(a => a)

    val numbersFlow2 = Flow[ConsumerMessage.TransactionalMessage[String, String]]
      .mapAsync(1) { n =>
        (if (Integer.valueOf(n.record.value()) == 8)
           Future.failed(new RuntimeException("bad luck"))
         //Future.successful(None)
         else Future.successful(Option(n.record.value()))).recover {
          case error: RuntimeException =>
            log.error("", error)
            None
        }
      }

    val producerFlow: Flow[
      (ConsumerMessage.TransactionalMessage[String, String], Option[String]),
      ProducerMessage.Envelope[String, String, ConsumerMessage.PartitionOffset],
      NotUsed
    ] =
      Flow[(ConsumerMessage.TransactionalMessage[String, String], Option[String])].map {
        asset: (ConsumerMessage.TransactionalMessage[String, String], Option[String]) =>
          asset._2 match {
            case Some(v) =>
              println("SOY   " + v)
              ProducerMessage.single(new ProducerRecord(sinkTopic, asset._1.record.key(), v), asset._1.partitionOffset)

            case None    =>
              println("PEPEPEPEPEPEPE")
              ProducerMessage.passThrough[String, String, PartitionOffset](asset._1.partitionOffset)
          }

      }

    val graph =
      GraphDSL
        .create() { implicit builder =>
          import GraphDSL.Implicits._

          // Step 2 - Add the necessary components of this graph
          val broadcast     = builder.add(Broadcast[ConsumerMessage.TransactionalMessage[String, String]](2))
          val zip           = builder.add(Zip[ConsumerMessage.TransactionalMessage[String, String], Option[String]]())
          val producerShape = builder.add(producerFlow)

          // Step 3 - Tying up the components
          broadcast ~> numbersFlow ~> zip.in0
          broadcast ~> numbersFlow2 ~> zip.in1
          zip.out ~> producerShape

          // Step 4 - Return a closed shape
          FlowShape(broadcast.in, producerShape.out)
        }
        .withAttributes(ActorAttributes.supervisionStrategy {
          // Resume = skips the faulty element
          // Stop = stop the stream
          // Restart = resume + clears internal state
          case e: RuntimeException =>
            println(e.getMessage)
            Restart
          case _                   => Stop
        })

    val source: (Consumer.Control, _) = Transactional
      .source(consumerSettings, Subscriptions.topics(Set(sourceTopic)))
      .log("Numbers Input")
      .via(graph)
      .log("Numbers Output")
      //.via(Transactional.flow(producerSettings, group))
      .viaMat(KillSwitches.single)(Keep.both)
      .toMat(Transactional.sink(producerSettings, group))(Keep.left)
      .run()

    // W H E N

    // T H E N
    val probeConsumerGroup = createGroupId(2)

    val (controlConsumer, probeConsumer) = createProbe(consumerSettings, sinkTopic)

    probeConsumer
      .request(10)
      .expectNextN((1 to 10).filterNot(_ == 8).map(_.toString))

    probeConsumer.cancel()
    Await.result(controlConsumer.shutdown(), remainingOrDefault)
    Await.result(source._1.shutdown(), remainingOrDefault)

  }

}

object NumbersKafkaIT {

  abstract class SpecBase(kafkaPort: Int) extends ScalatestKafkaSpec(kafkaPort) with AnyFlatSpecLike with Matchers with ScalaFutures with Eventually {

    protected def this() = this(kafkaPort = -1)

  }

}
