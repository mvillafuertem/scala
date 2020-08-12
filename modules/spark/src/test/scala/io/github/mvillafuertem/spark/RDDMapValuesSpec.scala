package io.github.mvillafuertem.spark

import org.apache.spark.{ SparkConf, SparkContext }
import org.scalatest.flatspec.AnyFlatSpecLike

final class RDDMapValuesSpec extends AnyFlatSpecLike {

  behavior of s"${this.getClass}"

  it should "Airports" in {

    val conf = new SparkConf().setAppName("airports").setMaster("local")
    val sc   = new SparkContext(conf)

    val resourcesPath = getClass.getResource("/airports.text")
    val airportsRDD   = sc.textFile(resourcesPath.getPath)

    val COMMA_DELIMITER = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"

    val airportPairRDD = airportsRDD.map(line => (line.split(COMMA_DELIMITER)(1), line.split(COMMA_DELIMITER)(3)))

    val airportsApplyFilter = airportPairRDD.mapValues(countryName => countryName.toUpperCase())

    airportsApplyFilter.saveAsTextFile("target/airports_map_value_uppercase.text")

  }

}
