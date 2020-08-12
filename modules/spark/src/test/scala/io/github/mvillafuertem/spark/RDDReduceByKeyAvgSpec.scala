package io.github.mvillafuertem.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.flatspec.AnyFlatSpecLike

final class RDDReduceByKeyAvgSpec extends AnyFlatSpecLike {

  behavior of s"${this.getClass}"

  it should "Word Counts" in {

    val conf = new SparkConf().setAppName("wordCounts").setMaster("local")
    val sc   = new SparkContext(conf)

    val resourcesPath = getClass.getResource("/word_count.text")
    val lines         = sc.textFile(resourcesPath.getPath)
    val wordRDD       = lines.flatMap(line => line.split(" "))
    val wordPairRDD   = wordRDD.map(word => (word, 1))

    val wordCounts = wordPairRDD.reduceByKey((x, y) => x + y)

    for ((word, count) <- wordCounts.collect()) println(word + " : " + count)

  }

}
