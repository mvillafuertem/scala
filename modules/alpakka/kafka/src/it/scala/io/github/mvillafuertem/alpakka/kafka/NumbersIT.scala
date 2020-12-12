package io.github.mvillafuertem.alpakka.kafka

import java.nio.charset.StandardCharsets

import akka.Done
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.{ AutoSubscription, Subscriptions }
import akka.stream.scaladsl.{ Keep, Source }
import akka.stream.testkit.scaladsl.TestSink
import io.github.mvillafuertem.alpakka.kafka.NumbersIT.NumbersConfigurationIT
import org.apache.kafka.clients
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * @author Miguel Villafuerte
 */
final class NumbersIT extends NumbersConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  it should "consume a event" in {

    // g i v e n
    val subscription: AutoSubscription = Subscriptions.topics(producerConfigurationProperties.producerTopic)

    // w h e n
    val (control, probe) = Consumer
      .plainSource(consumerSettings, subscription)
      .toMat(TestSink.probe)(Keep.both)
      .run()

    // t h e n
    val msg = "{\"id\":\"3771a4d1-477f-459c-9b64-90207e486992\",\"timestamp\":\"2019-03-18T15:28:07.000Z\"}"
    probe.ensureSubscription().requestNext().value() shouldBe msg
    control.shutdown().map(_ shouldBe Done)

  }

  override var container: containers.DockerComposeContainer[_] = _

  override protected def beforeAll(): Unit = {
    container = dockerInfrastructure
    container.start()
    Await.result(produce(), 60 seconds) shouldBe Done
  }

  override protected def afterAll(): Unit = container.stop()

}

object NumbersIT {

  trait NumbersConfigurationIT extends KafkaConfigurationIT with AsyncFlatSpecLike with Matchers with BeforeAndAfterAll {

    private val msg: String = scala.io.Source
      .fromURL(this.getClass.getResource("/event.json"))
      .getLines()
      .mkString
      .replaceAll("[\\n\\s]", "")

    def produce(): Future[Done] = {
      val _: clients.producer.Producer[String, String] = producerSettings.createKafkaProducer()
      Source(LazyList.from(1 to 1))
        .throttle(1, 10 milliseconds)
        .log("producer")
        .map { n =>
          val producer = new ProducerRecord[String, String](producerConfigurationProperties.producerTopic, msg)
          producer.headers().add("id", n.toString.getBytes(StandardCharsets.UTF_8))
          producer
        }
        .runWith(Producer.plainSink(producerSettings))
    }

  }

}
