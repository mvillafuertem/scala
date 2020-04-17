package io.github.mvillafuertem.akka.untyped.stream.advanced

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.util.{Failure, Success}

object SubStreams extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("SubStreams")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)
  import actorSystem.dispatcher

  // 1. Grouping a stream by a certain function
  val wordsSource = Source(List("Akka", "is", "amazing", "learning", "subStreams"))
  val groups = wordsSource.groupBy(30, word => if(word.isEmpty) '\u0000' else word.toLowerCase().charAt(0))

  groups.to(Sink.fold(0)((count, word) => {
    val newCount = count + 1
    println(s"I just received $word, count is $newCount")
    newCount
  }))
    //.run()


  // 2. Merge subStreams back
  val testSource = Source(List(
    "I love Akka Streams",
    "this is amazing",
    "learning Akka",
  ))

  val totalCharCountFuture = testSource
    .groupBy(2, string => string.length % 2)
    .map(_.length) // Do your expensive computation here
    .mergeSubstreams//WithParallelism(2)
    .toMat(Sink.reduce[Int](_ + _))(Keep.right)
    .run()


  totalCharCountFuture.onComplete {
    case Success(value) => println(s"Total char count $value")
    case Failure(exception) => println(s"Char computation failed $exception")
  }

  // 3. Splitting a stream into subStreams, when a condition is met

  val text =
    """
      |I love Akka Streams
      |this is amazing
      |learning akka
    """.stripMargin

}
