package io.github.mvillafuertem.spark.streaming

import org.apache.spark.sql.functions.{ col, length }
import org.apache.spark.sql.streaming.{ OutputMode, Trigger }
import org.apache.spark.sql.types.{ DateType, DoubleType, StringType, StructField, StructType }
import org.apache.spark.sql.{ DataFrame, SparkSession }
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ blocking, Await, Future }
import scala.concurrent.duration.{ Duration, DurationInt }

object StreamingDataFramesSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${this.getClass.getSimpleName}"

  it should "wip" in {}

}

object StreamingDataFrames {

  val spark: SparkSession = SparkSession
    .builder()
    .appName("Our first streams")
    .master("local[2]")
    .getOrCreate()

  def readFromSocket(): Unit = {

    // Reading a DF
    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345) // firstly typing in your terminal "nc -lk 12345"
      .load()

    // Transformation
    val shortLines = lines.filter(length(col("value")) <= 5)

    // Tell between a static vs streaming DF
    println(shortLines.isStreaming)

    // Consuming a DF
    val query = shortLines.writeStream
      .format("console")
      .outputMode("append")
      .start()

    // Wait for the stream to finish
    query.awaitTermination()

  }

  def readFromFiles(): Unit = {

    val stocksSchema = StructType(
      Array(
        StructField("company", StringType),
        StructField("date", DateType),
        StructField("value", DoubleType)
      )
    )

    val stocksDF: DataFrame = spark.readStream
      .format("csv")
      .option("header", false)
      .option("dateFormat", "MMM d yyyy")
      .schema(stocksSchema)
      .load("modules/spark/src/test/resources/data/stocks")

    stocksDF.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()

  }

  def demoTriggers() = {
    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345) // firstly typing in your terminal "nc -lk 12345"
      .load()

    // Write the lines DF at a certain trigger
    lines.writeStream
      .format("console")
      .outputMode("append")
      .trigger(
        // Trigger.ProcessingTime(2.seconds)// every 2 seconds run the query
        // Trigger.Once() // single batch, then terminate
        Trigger.Continuous(2.seconds) // experimental, every 2 seconds create a batch with whatever you have
      )
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit =
    // readFromSocket()
    // readFromFiles()
    demoTriggers()

}
