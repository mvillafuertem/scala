package io.github.mvillafuertem.benchmarks

import java.util
import java.util.concurrent.{ ConcurrentHashMap, TimeUnit }

import org.openjdk.jmh.annotations._

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class MapBenchmarks {

  private[this] var chm: ConcurrentHashMap[Int, String] = _
  private[this] var map: Map[Int, String]               = _
  private[this] var array: Array[String]                = _
  private[this] var jhm: util.Map[Int, String]          = _

  @Setup
  def setup(): Unit = {
    chm = new ConcurrentHashMap[Int, String]()
    chm.put(10, "hello!") // CHM internal array is lazily initialized

    map = Map(10 -> "hello")
    array = Array(null, null, "hello")

    jhm = new util.HashMap[Int, String]()
    jhm.put(10, "hello!")
  }

  @Benchmark
  def chmGet(): String =
    chm.get(10)

  @Benchmark
  def simGet(): Option[String] =
    map.get(10)

  @Benchmark
  def jhmGet(): String =
    jhm.get(10)

  @Benchmark
  def arrayGet(): String =
    array(2)
}
