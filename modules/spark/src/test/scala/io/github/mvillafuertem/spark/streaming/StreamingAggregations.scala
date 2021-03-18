package io.github.mvillafuertem.spark.streaming

import org.apache.spark.sql.{ Column, DataFrame, SparkSession }
import org.apache.spark.sql.functions.{ col, stddev, sum }

object StreamingAggregations {

  val spark = SparkSession
    .builder()
    .appName("Streaming Aggregations")
    .master("local[2]")
    .getOrCreate()

  def streamingCount(): Unit = {
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345) // firstly typing in your terminal "nc -lk 12345"
      .load()

    val lineCount = lines.selectExpr("count(*) as lineCount")

    // aggregations with distinct are not supported
    // otherwise Spark will need to keep track of EVERYTHING

    lineCount.writeStream
      .format("console")
      .outputMode("complete") // append and update not supported on aggregations without watermark
      .start()
      .awaitTermination()
  }

  def numericalAggregations(aggFunction: Column => Column): Unit = {
    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345) // firstly typing in your terminal "nc -lk 12345"
      .load()

    // aggregate here
    val numbers: DataFrame       = lines.select(col("value").cast("integer").as("number"))
    val aggregationDF: DataFrame = numbers.select(aggFunction(col("number")).as("agg_so_far"))

    aggregationDF.writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()

  }

  def groupNames(): Unit = {
    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345) // firstly typing in your terminal "nc -lk 12345"
      .load()

    // counting occurrences of the "name" value
    val names = lines
      .select(col("value").as("name"))
      .groupBy(col("name")) // RelationalGroupedDataset
      .count()

    names.writeStream
      .format("console")
      .outputMode("complete")
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit =
    // streamingCount()
    // numericalAggregations(stddev)
    groupNames()

}
