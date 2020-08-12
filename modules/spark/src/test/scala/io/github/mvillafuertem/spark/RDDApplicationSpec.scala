package io.github.mvillafuertem.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.flatspec.AnyFlatSpecLike

final class RDDReduceByKeySpec extends AnyFlatSpecLike {

  behavior of s"${this.getClass}"

  it should "Avg House Price" in {

    // g i v e n
    val conf = new SparkConf().setAppName("avgHousePrice").setMaster("local")
    val sc   = new SparkContext(conf)

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
