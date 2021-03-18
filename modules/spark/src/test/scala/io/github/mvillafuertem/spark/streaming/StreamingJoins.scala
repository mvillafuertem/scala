package io.github.mvillafuertem.spark.streaming

import org.apache.spark.sql.functions.{ col, from_json }
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{ Column, DataFrame, SparkSession }

object StreamingJoins {

  val spark: SparkSession = SparkSession
    .builder()
    .appName("Streamings Joins")
    .master("local[2]")
    .getOrCreate()

  val guitarPlayers: DataFrame = spark.read
    .option("inferedSchema", true)
    .json("modules/spark/src/test/resources/data/guitarPlayers")

  val guitars: DataFrame = spark.read
    .option("inferedSchema", true)
    .json("modules/spark/src/test/resources/data/guitars")

  val bands: DataFrame = spark.read
    .option("inferedSchema", true)
    .json("modules/spark/src/test/resources/data/bands")

  // joining static DFs
  val joinCondition: Column      = guitarPlayers.col("band") === bands.col("id")
  val guitaristsBands: DataFrame = guitarPlayers.join(bands, joinCondition, "inner")
  val bandsSchema: StructType    = bands.schema

  def joinStreamWithStatic() = {

    val streamedBandsDF: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load() // a DF with a single column "value" of type String
      .select(from_json(col("value"), bandsSchema).as("band"))
      .selectExpr("band.id as id", "band.name as name", "band.hometown as hometown", "band.year as year")

    val joinCondition: Column     = guitarPlayers.col("band") === streamedBandsDF.col("id")
    val streamedBandsGuitaristsDF = streamedBandsDF.join(guitarPlayers, joinCondition, "inner")

    // Restricted Joins
    // - stream joining with static: RIGHT outer join/full outer join/right_semi not permitted
    // - static joining with streaming: LEFT outer join/full/left_semi not permitted
    streamedBandsGuitaristsDF.writeStream
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()

  }

  // since Spark 2.3 we have have stream vs stream joins
  def joinStreamWithStream() = {
    val streamedBandsDF: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12345)
      .load() // a DF with a single column "value" of type String
      .select(from_json(col("value"), bandsSchema).as("band"))
      .selectExpr("band.id as id", "band.name as name", "band.hometown as hometown", "band.year as year")

    val streamedGuitaristDF: DataFrame = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 12346)
      .load() // a DF with a single column "value" of type String
      .select(from_json(col("value"), guitarPlayers.schema).as("guitarPlayer"))
      .selectExpr("guitarPlayer.id as id", "guitarPlayer.name as name", "guitarPlayer.guitars as guitars", "guitarPlayer.band as band")

    val joinCondition: Column = streamedGuitaristDF.col("band") === streamedBandsDF.col("id")

    // join stream with stream
    val streamedJoin = streamedBandsDF.join(streamedGuitaristDF, joinCondition, "inner")

    // - inner joins are supported
    // - left/right outer joins are supported, but must have watermarks
    // - full outer joins are NOT supported
    streamedJoin.writeStream
      .format("console")
      .outputMode("append") // only append supported for stream vs stream join
      .start()
      .awaitTermination()
  }

  def main(args: Array[String]): Unit =
    joinStreamWithStream()

}
