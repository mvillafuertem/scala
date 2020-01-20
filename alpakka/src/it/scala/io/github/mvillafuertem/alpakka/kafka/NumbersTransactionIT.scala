package io.github.mvillafuertem.alpakka.kafka

import java.io.File
import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.ConsumerMessage.PartitionOffset
import akka.kafka.scaladsl.{Consumer, Producer, Transactional}
import akka.kafka.{ConsumerMessage, ProducerMessage, Subscriptions}
import akka.stream.Supervision.{Restart, Stop}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Source, Zip}
import akka.stream.testkit.scaladsl.StreamTestKit.assertAllStagesStopped
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorAttributes, ActorMaterializer, FlowShape}
import akka.{Done, NotUsed}
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import io.github.mvillafuertem.alpakka.kafka.NumbersTransactionIT.NumbersTransactionConfigurationIT
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.testcontainers.containers
import org.testcontainers.containers.wait.strategy.Wait

import scala.collection.immutable
import scala.concurrent.{ExecutionContextExecutor, Future}

class NumbersTransactionIT extends NumbersTransactionConfigurationIT
  with EventConfiguration
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("KafkaConnectionCheckerSpec")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val sinkTopic = "sink-topic"
  val sourceTopic = "source-topic"

  val producerSettings = createProducerSettings(KafkaProducerConfigurationProperties())
  val consumerSettings = createConsumerSettings(KafkaConsumerConfigurationProperties())

  it should "numbers" in assertAllStagesStopped {

    // G I V E N
    val numbersFlow = Flow[ConsumerMessage.TransactionalMessage[String, String]]
      .map(a => a)

    val numbersFlow2 = Flow[ConsumerMessage.TransactionalMessage[String, String]]
      .mapAsync(1) {n =>
        (if (Integer.valueOf(n.record.value()) == 8)
          Future.failed(new RuntimeException("bad luck"))
        //Future.successful(None)
        else Future.successful(Option(n.record.value()))).recover {
          case exception: RuntimeException =>
            log.error("", exception)
            None
          case exception@_ =>
            log.error("", exception)
            None
        }
      }


    val producerFlow:
      Flow[(ConsumerMessage.TransactionalMessage[String, String], Option[String]),
        ProducerMessage.Envelope[String, String, ConsumerMessage.PartitionOffset],
        NotUsed] =
      Flow[(ConsumerMessage.TransactionalMessage[String, String], Option[String])]
        .map{asset: (ConsumerMessage.TransactionalMessage[String, String], Option[String]) =>

          asset._2 match {
            case Some(v) => println("SOY   " + v)
              ProducerMessage.single(new ProducerRecord(sinkTopic, asset._1.record.key(), v), asset._1.partitionOffset)

            case None => println("PEPEPEPEPEPEPE")
              ProducerMessage.passThrough[String, String, PartitionOffset](asset._1.partitionOffset)
          }

        }


    val graph =
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        // Step 2 - Add the necessary components of this graph
        val broadcast = builder.add(Broadcast[ConsumerMessage.TransactionalMessage[String, String]](2))
        val zip = builder.add(Zip[ConsumerMessage.TransactionalMessage[String, String], Option[String]])
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
          case e@_ =>
            println(e.getMessage)
            Stop
        })


    Transactional
      .source(consumerSettings, Subscriptions.topics(Set(sourceTopic)))
      .log("Numbers Input")
      .via(graph)
      .log("Numbers Output")
      .toMat(Transactional.sink(producerSettings, KafkaProducerConfigurationProperties().transactionalId))(Keep.both)
      .run()

    // W H E N

    // T H E N
    val probeConsumer = Consumer
      .plainSource(consumerSettings, Subscriptions.topics(Set(sinkTopic)))
      .map(r => (r.offset(), r.value()))
      .map(_._2)
      .runWith(TestSink.probe)

    probeConsumer
      .request(9)
      .expectNextN(
        immutable.Seq("1", "2", "3", "4", "5", "6", "7", "9", "10"))

    probeConsumer.cancel()

  }

  def produce(): Unit = {

    val kafkaProducer = producerSettings.createKafkaProducer()

    val eventualDone = Source(LazyList.from(1 to 10))
      .map(s => {
        Thread.sleep(1)
        log.info("{}", s)
        s
      })
      .map(value => {
        val str = String.valueOf(value)
        log.info("{}", str)

        val producer = new ProducerRecord[String, String](sourceTopic, str)

        producer.headers().add("id", "1234567890".getBytes(StandardCharsets.UTF_8))

        producer
      })
      .runWith(Producer.plainSink(producerSettings))

    Thread.sleep(6000)
    eventualDone.futureValue shouldBe Done

  }

  override protected def beforeAll(): Unit = {

    dockerInfrastructure.start()
    produce()
  }

  override protected def afterAll(): Unit = dockerInfrastructure.stop()

}

object NumbersTransactionIT {

  trait NumbersTransactionConfigurationIT {

    val dockerInfrastructure: containers.DockerComposeContainer[_]  = DockerComposeContainer(
      new File("alpakka/src/it/resources/docker-compose.it.yml"),
      exposedServices = Seq(ExposedService("kafka", 9092, 1,  Wait.forLogMessage(".*started .*\\n", 1))),
      identifier = "docker_infrastructure"
    ).container

  }
}

