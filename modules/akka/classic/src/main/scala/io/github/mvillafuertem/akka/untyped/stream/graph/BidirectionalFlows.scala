package io.github.mvillafuertem.akka.untyped.stream.graph

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, GraphDSL, RunnableGraph, Sink, Source }
import akka.stream.{ BidiShape, ClosedShape, Materializer }

object BidirectionalFlows extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("BidirectionalFlows")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)

  /**
   * Example: Cryptography
   */
  def encrypt(n: Int)(string: String) = string.map(c => (c + n).toChar)
  def decrypt(n: Int)(string: String) = string.map(c => (c - n).toChar)

  // BidiFlow
  val bidiCryptoStaticGraph = GraphDSL.create() { implicit builder =>
    val encryptionFlowShape = builder.add(Flow[String].map(encrypt(3)))
    val decryptionFlowShape = builder.add(Flow[String].map(decrypt(3)))

    // BidiShape(encryptionFlowShape.in, encryptionFlowShape.out, decryptionFlowShape.in, decryptionFlowShape.out)
    BidiShape.fromFlows(encryptionFlowShape, decryptionFlowShape)
  }

  val unencryptedStrings = List("akka", "is", "awesome", "testing", "bidirectional", "flows")
  val unencryptedSource  = Source(unencryptedStrings)
  val encryptedSource    = Source(unencryptedStrings.map(encrypt(3)))

  val cryptoBidiGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val unencryptedSourceShape = builder.add(unencryptedSource)
      val encryptedSourceShape   = builder.add(encryptedSource)
      val bidi                   = builder.add(bidiCryptoStaticGraph)
      val encryptedSinkShape     = builder.add(Sink.foreach[String](string => println(s"Encrypted: $string")))
      val decryptedSinkShape     = builder.add(Sink.foreach[String](string => println(s"Decrypted: $string")))

      unencryptedSourceShape ~> bidi.in1; bidi.out1 ~> encryptedSinkShape
      decryptedSinkShape <~ bidi.out2; bidi.in2 <~ encryptedSourceShape

      ClosedShape
    }
  )

  cryptoBidiGraph.run()

}
