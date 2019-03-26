package io.github.mvillafuertem.scala.akka.stream.init

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}


object MaterializingStreams extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("FirstPrinciples")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  import actorSystem.dispatcher

  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
  val simpleMaterializedValue = simpleGraph.run()

  val source = Source(1 to 10)
  val sink = Sink.reduce[Int]((a, b) => a + b)
  val sumFuture = source.runWith(sink)

  sumFuture.onComplete {
    case Success(value) => println(s"The sum of all elements is : $value")
    case Failure(exception) => println(s"The sum of the elements could not be computed: $exception")
  }

  // Choosing materialized values
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach[Int](println)

  simpleSource.viaMat(simpleFlow)((sourceMat, flowMat) => flowMat)
  simpleSource.viaMat(simpleFlow)(Keep.right)
  simpleSource.viaMat(simpleFlow)(Keep.left)
  simpleSource.viaMat(simpleFlow)(Keep.both)

  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
  graph.run().onComplete {
    case Success(_) => println("Stream proccesing finished")
    case Failure(exception) => println(s"Stream processing failed with $exception")
  }

  // Syntactic Sugar
  // source.to(Sink.reduce)(Keep.right)
  val sum = Source(1 to 10).runWith(Sink.reduce[Int](_ + _))
  Source(1 to 10).runReduce[Int](_ + _)

  // Backwards
  // source(..).to(sink..).run()
  Sink.foreach[Int](println).runWith(Source.single(42))

  // Both ways
  Flow[Int].map(x => x * 2).runWith(simpleSource, simpleSink)

  /**
    * Return the last element out of a source (use Sink.last)
    * Compute the total word count of a stream of sentences (map, fold, reduce)
    */

  val sentences = List("Hello Word", "Hello Jupiter", "Hello Mars")
  val sentencesSource = Source(sentences)

  val f1 = Source(1 to 10).toMat(Sink.last)(Keep.right).run()
  val f2 = Source(1 to 10).runWith(Sink.last)

  val wordCountSink = Sink.fold[Int, String](0)((currentWord, newSentence) => currentWord + newSentence.split(" ").length)
  val g1 = sentencesSource.toMat(wordCountSink)(Keep.right).run()
  val g2 = sentencesSource.runWith(wordCountSink)
  val g3 = sentencesSource.runFold(0)((currentWord, newSentence) => currentWord + newSentence.split(" ").length)

  val wordCountFlow = Flow[String].fold[Int](0)((currentWord, newSentence) => currentWord + newSentence.split(" ").length)
  val g4 = sentencesSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val g5 = sentencesSource.viaMat(wordCountFlow)(Keep.left).toMat(Sink.head)(Keep.right).run()
  val g6 = sentencesSource.via(wordCountFlow).runWith(Sink.head)
  val g7 = wordCountFlow.runWith(sentencesSource, Sink.head)._2
}
