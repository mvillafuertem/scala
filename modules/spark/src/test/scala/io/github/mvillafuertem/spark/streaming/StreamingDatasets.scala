package io.github.mvillafuertem.spark.streaming

import org.apache.spark.sql.{ DataFrame, Dataset, Encoders, SparkSession }
import org.apache.spark.sql.functions.{ col, from_json }
import org.apache.spark.sql.types._

object StreamingDatasets {

  val spark                    = SparkSession
    .builder()
    .appName("Streaming Datasets")
    .master("local[2]")
    .getOrCreate()

  case class Car(
    Name: String,
    Miles_per_Gallon: Option[Double],
    Cylinders: Option[Long],
    Displacement: Option[Double],
    Horsepower: Option[Long],
    Weight_in_lbs: Option[Long],
    Acceleration: Option[Double],
    Year: String,
    Origin: String
  )
  def readCars(): Dataset[Car] = {

    val carsSchema = StructType(
      Array(
        StructField("Name", StringType),
        StructField("Miles_per_Gallon", DoubleType),
        StructField("Cylinders", LongType),
        StructField("Displacement", DoubleType),
        StructField("Horsepower", LongType),
        StructField("Weight_in_lbs", LongType),
        StructField("Acceleration", DoubleType),
        StructField("Year", StringType),
        StructField("Origin", StringType)
      )
    )

    spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load()                                                // DF with single string column "value"
      .select(from_json(col("value"), carsSchema).as("car")) // composite column (struct)
      .selectExpr("car.*")                                   // DF with multiple columns
      .as[Car](Encoders.product[Car]) // or import spark.implicits._ include encoders for DF -> DS transformations

  }

  def showCarsNames() = {
    val cardsDS: Dataset[Car] = readCars()

    // transformations here
    // val carNamesDF: DataFrame = cardsDS.select(col("Name")) // Here DataFrame without type

    // collections transformation maintain type info
    import spark.implicits._
    val carNamesAlt: Dataset[String] = cardsDS.map(_.Name)

    carNamesAlt.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def ex1() = {
    val carsDS = readCars()
    carsDS
      .filter(_.Horsepower.getOrElse(0L) > 140)
      .writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit = ex1()

}
