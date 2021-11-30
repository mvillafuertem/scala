package io.github.mvillafuertem.akka.untyped.stream.advanced

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ BroadcastHub, Keep, MergeHub, Sink, Source }
import akka.stream.{ KillSwitches, Materializer }

import scala.concurrent.duration._

object DynamicStreamHandling extends App {

  implicit val actorSystem: ActorSystem        = ActorSystem("DynamicStreamHandling")
  implicit val actorMaterializer: Materializer = Materializer(actorSystem)
  import actorSystem.dispatcher

  // 1. Kill Switch
  val killSwitchFlow = KillSwitches.single[Int]
  val counter        = Source(LazyList.from(1)).throttle(1, 1 second).log("counter")
  val sink           = Sink.ignore

  //  val killSwitch = counter
  //    .viaMat(killSwitchFlow)(Keep.right)
  //    .to(sink)
  //    .run()
  //
  //  actorSystem.scheduler.scheduleOnce(3 seconds) {
  //    killSwitch.shutdown()
  //  }

  val anotherCounter   =
    Source(LazyList.from(1)).throttle(2, 1 second).log("anotherCounter")
  val sharedKillSwitch = KillSwitches.shared("oneButtonToRuleThemAll")

  counter.via(sharedKillSwitch.flow).runWith(Sink.ignore)
  anotherCounter.via(sharedKillSwitch.flow).runWith(Sink.ignore)

  actorSystem.scheduler.scheduleOnce(3 seconds) {
    sharedKillSwitch.shutdown()
  }

  // MergeHub
  val dynamicMerge     = MergeHub.source[Int]
  val materializedSink = dynamicMerge.to(Sink.foreach[Int](println)).run()

  // Use this sink any time we like
  Source(1 to 10).runWith(materializedSink)
  counter.runWith(materializedSink)

  // BroadcastHub
  val dynamicBroadcast   = BroadcastHub.sink[Int]
  val materializedSource = Source(1 to 100).runWith(dynamicBroadcast)

  materializedSource.runWith(Sink.ignore)
  materializedSource.runWith(Sink.foreach[Int](println))

  /**
   * Combine a mergeHub and a broadcastHub
   *
   * A publisher-subscriber component
   */
  val merge                           = MergeHub.source[String]
  val broadcast                       = BroadcastHub.sink[String]
  val (publisherPort, subscriberPort) = merge.toMat(broadcast)(Keep.both).run()

  subscriberPort.runWith(Sink.foreach(e => println(s"I received $e")))
  subscriberPort
    .map(string => string.length)
    .runWith(Sink.foreach(n => println(s"I got a number $n")))

  Source(List("Akka", "is", "amazing")).runWith(publisherPort)
  Source(List("I", "love", "Scala")).runWith(publisherPort)
  Source.single("Yeah!").runWith(publisherPort)

}
