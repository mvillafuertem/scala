package io.github.mvillafuertem.alpakka.kafka

import java.io.File
import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConnectionCheckerSettings, ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import com.dimafeng.testcontainers.DockerComposeContainer
import io.github.mvillafuertem.alpakka.kafka.NumbersIT.NumbersConfigurationIT
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.testcontainers.containers.wait.strategy.Wait

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
 * @author Miguel Villafuerte
 */
final class NumbersIT extends NumbersConfigurationIT
with EventConfiguration
  with FlatSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("KafkaConnectionCheckerSpec")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val log: LoggingAdapter = Logging(system, this.getClass)
  implicit val mat: ActorMaterializer = ActorMaterializer()

  behavior of "Numbers IT"

  val retryInterval: FiniteDuration = 100.millis
  val connectionCheckerConfig: ConnectionCheckerSettings = ConnectionCheckerSettings(1, retryInterval, 2d)
  val producerSettings: ProducerSettings[String, String] = createProducerSettings(KafkaProducerConfigurationProperties())
  val consumerSettings: ConsumerSettings[String, String] = createConsumerSettings(KafkaConsumerConfigurationProperties())
  val failingDetectionTime: FiniteDuration = 10.seconds

  val topic = "sink-topic"

  it should "consume a event" in {

      val (control, probe) =
        Consumer.plainSource(consumerSettings, Subscriptions.topics(topic)).toMat(TestSink.probe)(Keep.both).run

      val msg = "{\"id\":\"3771a4d1-477f-459c-9b64-90207e486992\",\"timestamp\":\"2019-03-18T15:28:07.000Z\"}"
      probe.ensureSubscription().requestNext().value() shouldBe msg
   }

  private val msg: String = scala.io.Source.fromURL(getClass.getResource("/event.json"))
    .getLines()
    .mkString
    .replaceAll("[\\n\\s]", "")

  def produce(): Unit = {

    val kafkaProducer = producerSettings.createKafkaProducer()

    val eventualDone = Source(LazyList.from(1 to 1))
      .map(s => {
        Thread.sleep(1)
        log.info("{}", msg)
        msg
      })
      .map(value => {

        val producer = new ProducerRecord[String, String](topic, value)

        producer.headers().add("id", "1234567890".getBytes(StandardCharsets.UTF_8))

        producer
      })
      .runWith(Producer.plainSink(producerSettings, kafkaProducer))

  }

  override protected def beforeAll(): Unit = {

    dockerInfrastructure.start()
    produce()
  }

  override protected def afterAll(): Unit = dockerInfrastructure.stop()

}

object NumbersIT {


  trait NumbersConfigurationIT {

    val dockerInfrastructure = DockerComposeContainer(
      new File("alpakka/src/it/resources/docker-compose.it.yml"),
      identifier = "docker_infrastructure"
    ).container
      .waitingFor("kafka_1", Wait.forLogMessage(".*started .*\\n", 1))

  }

}


