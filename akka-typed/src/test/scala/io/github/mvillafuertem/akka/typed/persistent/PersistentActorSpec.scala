package io.github.mvillafuertem.akka.typed.persistent

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.stream.scaladsl.Source
import akka.stream.typed.javadsl.ActorSink
import com.typesafe.config.{Config, ConfigFactory}
import io.github.mvillafuertem.akka.typed.persistent.PersistentActor.{AddPerson, GetPerson, ModifyLastName, Person}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._


/**
  * @author Miguel Villafuerte
  */
final class PersistentActorSpec extends ScalaTestWithActorTestKit(PersistentActorSpec.conf)
  with FlatSpecLike with Matchers {

  behavior of "Persistent Actor"

  it should "add person" in {

    // g i v e n
    val person = Person("Pepe", "Pipo")
    val value = spawn(PersistentActor.supervisedBehavior("id"))
    val probe = TestProbe[Person]

    // w h e n
    value ! AddPerson(person)
    value ! GetPerson(probe.ref)

    // t h e n
    probe.expectMessage(person)

  }

  it should "modify person" in {
    // g i v e n
    val person = Person("Pepe", "Pipo")
    val value = spawn(PersistentActor.supervisedBehavior("id"))
    val probe = TestProbe[Person]

    // w h e n
    value ! AddPerson(person)
    value ! ModifyLastName("Popu")
    value ! GetPerson(probe.ref)

    // t h e n
    probe.expectMessage(Person("Pepe", "Popu"))
  }

  it should "integrate with streams source person" in {
    // g i v e n
    val person = Person("Pepe", "Pipo")
    val value = spawn(PersistentActor.supervisedBehavior("id"))
    val probe = TestProbe[Person]

    val actorSink = ActorSink.actorRef[PersistentActor.Command](value.ref,
      GetPerson(probe.ref),
      _ => AddPerson(person))

    Source(1 to 10)
      .throttle(1, 1 second)
      .map(n => Person(s"Pepe $n", "Pipo"))
      .log("source")
      .map(p => AddPerson(p))
      .to(actorSink)
      .run()


    // w h e n
    value ! AddPerson(person)
    value ! ModifyLastName("Popu")
    value ! GetPerson(probe.ref)

    // t h e n
    probe.expectMessage(Person("Pepe", "Popu"))
  }

}

object PersistentActorSpec {

  def conf: Config = ConfigFactory.parseString(s"""
    akka.loglevel = INFO
    akka.loggers = [akka.testkit.TestEventListener]
    akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
    akka.persistence.journal.inmem.test-serialization = on
    akka.actor.warn-about-java-serializer-usage = false
    akka.actor.allow-java-serialization = on
    """)

}
