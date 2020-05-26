//package io.github.mvillafuertem.zio.kafka
//
//import org.apache.kafka.clients.producer.ProducerRecord
//import zio.blocking.Blocking
//import zio.kafka.producer.{ Producer, ProducerSettings }
//import zio.kafka.serde.Serde
//import zio.{ App, Chunk, ZIO }
//
//object ProducerApp extends App {
//
//  val producerSettings: ProducerSettings = ProducerSettings(List("localhost:9092"))
//
//  val producerLayer = Producer.make(producerSettings, Serde.int, Serde.string).toLayer
//
//  val chunks = Chunk
//    .fromIterable(1 to 100000)
//    .map(n => new ProducerRecord("my-topic", n, s"${n}"))
//
//  val producer = Producer
//    .produceChunk[Any, Int, String](chunks)
//    .provideSomeLayer[Blocking](producerLayer)
//    .fold(_ => 1, _ => 0)
//
//  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = producer
//
//}
