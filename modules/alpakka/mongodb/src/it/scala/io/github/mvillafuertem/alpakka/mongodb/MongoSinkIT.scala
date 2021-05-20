package io.github.mvillafuertem.alpakka.mongodb

import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import akka.stream.alpakka.mongodb.{ DocumentReplace, DocumentUpdate }
import com.mongodb.reactivestreams.client.MongoDatabase
import io.github.mvillafuertem.alpakka.mongodb.MongoSinkIT.MongoSinkConfigurationIT
import org.scalatest.flatspec.AnyFlatSpecLike
import org.testcontainers.containers
//import akka.stream.alpakka.testkit.scaladsl.LogCapturing
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.testkit.scaladsl.StreamTestKit.assertAllStagesStopped
import com.mongodb.client.model.{ Filters, InsertManyOptions, Updates }
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries.{ fromProviders, fromRegistries }
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

final class MongoSinkIT extends MongoSinkConfigurationIT {

  behavior of s"${this.getClass.getSimpleName}"

  it should "save with insertOne" in assertAllStagesStopped {
    val source     = Source(testRange).map(i => Document.parse(s"""{"value":$i}"""))
    val completion = source.runWith(MongoSink.insertOne(numbersDocumentColl))

    completion.futureValue

    val found = Source.fromPublisher(numbersDocumentColl.find()).runWith(Sink.seq).futureValue

    found.map(_.getInteger("value")) must contain theSameElementsAs testRange
  }

  it should "save with insertOne and codec support" in assertAllStagesStopped {
    // #insert-one
    val testRangeObjects = testRange.map(Number)
    val source           = Source(testRangeObjects)
    source.runWith(MongoSink.insertOne(numbersColl)).futureValue
    // #insert-one

    val found = Source.fromPublisher(numbersColl.find()).runWith(Sink.seq).futureValue

    found must contain theSameElementsAs testRangeObjects
  }

  it should "save with insertMany" in assertAllStagesStopped {
    val source = Source(testRange).map(i => Document.parse(s"""{"value":$i}"""))

    source.grouped(2).runWith(MongoSink.insertMany(numbersDocumentColl)).futureValue

    val found = Source.fromPublisher(numbersDocumentColl.find()).runWith(Sink.seq).futureValue

    found.map(_.getInteger("value")) must contain theSameElementsAs testRange
  }

  it should "save with insertMany and codec support" in assertAllStagesStopped {
    // #insert-many
    val objects    = testRange.map(Number)
    val source     = Source(objects)
    val completion = source.grouped(2).runWith(MongoSink.insertMany[Number](numbersColl))
    // #insert-many

    completion.futureValue

    val found = Source.fromPublisher(numbersColl.find()).runWith(Sink.seq).futureValue

    found must contain theSameElementsAs objects
  }

  it should "save with insertMany with options" in assertAllStagesStopped {
    val source = Source(testRange).map(i => Document.parse(s"""{"value":$i}"""))

    source
      .grouped(2)
      .runWith(MongoSink.insertMany(numbersDocumentColl, new InsertManyOptions().ordered(false)))
      .futureValue

    val found = Source.fromPublisher(numbersDocumentColl.find()).runWith(Sink.seq).futureValue

    found.map(_.getInteger("value")) must contain theSameElementsAs testRange
  }

  it should "save with insertMany with options and codec support" in assertAllStagesStopped {
    val testRangeObjects = testRange.map(Number)
    val source           = Source(testRangeObjects)

    source
      .grouped(2)
      .runWith(MongoSink.insertMany[Number](numbersColl, new InsertManyOptions().ordered(false)))
      .futureValue

    val found = Source.fromPublisher(numbersColl.find()).runWith(Sink.seq).futureValue

    found must contain theSameElementsAs testRangeObjects
  }

  it should "update with updateOne" in assertAllStagesStopped {
    insertTestRange()

    // #update-one
    val source     = Source(testRange).map(i => DocumentUpdate(filter = Filters.eq("value", i), update = Updates.set("updateValue", i * -1)))
    val completion = source.runWith(MongoSink.updateOne(numbersDocumentColl))
    // #update-one

    completion.futureValue

    val found = Source.fromPublisher(numbersDocumentColl.find()).runWith(Sink.seq).futureValue

    found.map(doc => doc.getInteger("value") -> doc.getInteger("updateValue")) must contain theSameElementsAs testRange
      .map(i => i -> i * -1)
  }

  it should "update with updateMany" in assertAllStagesStopped {
    insertTestRange()

    val source = Source
      .single(0)
      .map(_ => DocumentUpdate(filter = Filters.gte("value", 0), update = Updates.set("updateValue", 0)))

    source.runWith(MongoSink.updateMany(numbersDocumentColl)).futureValue

    val found = Source.fromPublisher(numbersDocumentColl.find()).runWith(Sink.seq).futureValue

    found.map(doc => doc.getInteger("value") -> doc.getInteger("updateValue")) must contain theSameElementsAs testRange
      .map(i => i -> 0)
  }

  it should "delete with deleteOne" in assertAllStagesStopped {
    insertTestRange()

    // #delete-one
    val source     = Source(testRange).map(i => Filters.eq("value", i))
    val completion = source.runWith(MongoSink.deleteOne(numbersDocumentColl))
    // #delete-one

    completion.futureValue

    val found = Source.fromPublisher(numbersDocumentColl.find()).runWith(Sink.seq).futureValue

    found mustBe empty
  }

  it should "delete with deleteMany" in assertAllStagesStopped {
    insertTestRange()

    val source = Source.single(0).map(_ => Filters.gte("value", 0))

    source.runWith(MongoSink.deleteMany(numbersDocumentColl)).futureValue

    val found = Source.fromPublisher(numbersDocumentColl.find()).runWith(Sink.seq).futureValue

    found mustBe empty
  }

  it should "replace with replaceOne and codec support" in assertAllStagesStopped {
    insertDomainObjectsRange()
    val updatedObjects =
      testRange.map(i => DomainObject(i, s"updated-first-property-$i", s"updated-second-property-$i"))

    // #replace-one
    val source     = Source(testRange).map(i =>
      DocumentReplace[DomainObject](
        filter = Filters.eq("_id", i),
        replacement = DomainObject(i, s"updated-first-property-$i", s"updated-second-property-$i")
      )
    )
    val completion = source.runWith(MongoSink.replaceOne[DomainObject](domainObjectsColl))
    // #replace-one

    completion.futureValue

    val found = Source.fromPublisher(domainObjectsColl.find()).runWith(Sink.seq).futureValue

    found must contain theSameElementsAs updatedObjects
  }

}

object MongoSinkIT {

  trait MongoSinkConfigurationIT
      extends MongoDBConfigurationIT
      with AnyFlatSpecLike
      with ScalaFutures
      with BeforeAndAfterEach
      with BeforeAndAfterAll
      with Matchers {

    // case class and codec for mongodb macros
    case class Number(_id: Int)
    case class DomainObject(_id: Int, firstProperty: String, secondProperty: String)

    val codecRegistry = fromRegistries(fromProviders(classOf[Number], classOf[DomainObject]), DEFAULT_CODEC_REGISTRY)

    override var container: containers.DockerComposeContainer[_] = _

    override protected def beforeAll(): Unit = {
      container = dockerInfrastructure
      container.start()
      Source.fromPublisher(db.drop()).runWith(Sink.head).futureValue
    }

    val db: MongoDatabase                                    = client.getDatabase("MongoSinkSpec").withCodecRegistry(codecRegistry)
    val numbersColl: MongoCollection[Number]                 =
      db.getCollection("numbersSink", classOf[Number]).withCodecRegistry(codecRegistry)
    val numbersDocumentColl: MongoCollection[Document]       = db.getCollection("numbersSink")
    val domainObjectsColl: MongoCollection[DomainObject]     =
      db.getCollection("domainObjectsSink", classOf[DomainObject]).withCodecRegistry(codecRegistry)
    val domainObjectsDocumentColl: MongoCollection[Document] = db.getCollection("domainObjectsSink")

    implicit val defaultPatience: PatienceConfig =
      PatienceConfig(timeout = 20.seconds, interval = 200.millis)

    override def afterEach(): Unit = {
      Source.fromPublisher(numbersDocumentColl.deleteMany(new Document())).runWith(Sink.head).futureValue
      Source.fromPublisher(domainObjectsDocumentColl.deleteMany(new Document())).runWith(Sink.head).futureValue
    }

    override protected def afterAll(): Unit = {
      actorSystem.terminate().futureValue
      container.stop()
    }

    val testRange: Seq[Int] = 0 until 10

    def insertTestRange(): Unit =
      Source
        .fromPublisher(numbersDocumentColl.insertMany(testRange.map(i => Document.parse(s"""{"value":$i}""")).asJava))
        .runWith(Sink.head)
        .futureValue

    def insertDomainObjectsRange(): Unit =
      Source
        .fromPublisher(
          domainObjectsColl.insertMany(
            testRange.map(i => DomainObject(i, s"first-property-$i", s"second-property-$i")).asJava
          )
        )
        .runWith(Sink.head)
        .futureValue
  }

}
