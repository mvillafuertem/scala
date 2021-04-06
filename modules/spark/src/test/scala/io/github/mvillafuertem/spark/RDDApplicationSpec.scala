package io.github.mvillafuertem.spark

import java.net.URL

import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkConf, SparkContext }
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class RDDApplicationSpec extends AnyFlatSpecLike with Matchers {

  behavior of s"${this.getClass}"

  val conf: SparkConf  = new SparkConf().setAppName("RDDApplicationSpec").setMaster("local").set("spark.driver.host", "127.0.0.1")
  val sc: SparkContext = new SparkContext(conf)

  it should "Filter Airports" in {

    // g i v e n
    val COMMA_DELIMITER: String = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"
    val resourcesPath: URL      = getClass.getResource("/airports.text")

    // w h e n
    val actual: RDD[(String, String)] = sc
      .textFile(resourcesPath.getPath)
      .map(line => (line.split(COMMA_DELIMITER)(1), line.split(COMMA_DELIMITER)(3)))
      .filter(k => k._2 != "\"United States\"")

    // t h e n
    actual.count() shouldBe 6410
    //actual.saveAsTextFile("target/airports_not_in_usa_pair_rdd.txt")

  }

  it should "MapValues Airports" in {

    // g i v e n
    val COMMA_DELIMITER = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"
    val resourcesPath   = getClass.getResource("/airports.text")

    // w h e n
    val actual: RDD[(String, String)] = sc
      .textFile(resourcesPath.getPath)
      .map(line => (line.split(COMMA_DELIMITER)(1), line.split(COMMA_DELIMITER)(3)))
      .mapValues(countryName => countryName.toUpperCase())

    // t h e n
    actual.count() shouldBe 8107
    //actual.saveAsTextFile("target/airports_map_value_uppercase.text")

  }

  it should "ReduceByKey Word Counts" in {

    // g i v e n
    val resourcesPath: URL = getClass.getResource("/word_count.text")

    // w h e n
    val actual: RDD[(String, Int)] = sc
      .textFile(resourcesPath.getPath)
      .flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .reduceByKey((x, y) => x + y)

    // t h e n
    actual.count() shouldBe 376
    for ((word, count) <- actual.collect()) println(word + " : " + count)

  }

  it should "ReduceByKey Avg House Price" in {

    // g i v e n
    val resourcesPath = getClass.getResource("/RealEstate.csv")
    val lines         = sc.textFile(resourcesPath.getPath)
    val cleanedLines  = lines.filter(line => !line.contains("Bedrooms"))

    // w h e n
    val housePricePairRDD = cleanedLines.map(line => (line.split(",")(3), (1, line.split(",")(2).toDouble)))

    val housePriceTotal = housePricePairRDD.reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2))
    println("HousePriceTotal: ")
    for ((bedroom, total) <- housePriceTotal.collect()) println(bedroom + " : " + total)

    val housePriceAvg = housePriceTotal.mapValues(avgCount => avgCount._2 / avgCount._1)
    println("HousePriceAvg: ")

    // t h e n
    for ((bedroom, avg) <- housePriceAvg.collect()) println(bedroom + " : " + avg)

  }

}
