package io.github.mvillafuertem.akka.todo.infrastructure

import java.util.{Date, UUID}

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import com.typesafe.config.{Config, ConfigFactory}
import io.github.mvillafuertem.akka.todo.domain.ToDo
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoBehavior.{Close, GetToDo, Open, State}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers, OneInstancePerTest}

/**
 * @author Miguel Villafuerte
 */
final class ToDoBehaviorSpec extends ScalaTestWithActorTestKit(ToDoBehaviorSpec.conf)
  with FlatSpecLike
  with Matchers
  with BeforeAndAfterAll
  with OneInstancePerTest {

  behavior of "ToDo Behavior Spec"

  it should "open" in {

    // G I V E N
    val toDo = ToDo("ToDo", "ToDo with id 1234567890", new Date().toInstant.toEpochMilli)
    val value = spawn(ToDoBehavior(s"ToDo-${toDo.id}"))
    val probe = TestProbe[State]

    // W H E N
    value ! Open(toDo)
    value ! GetToDo(probe.ref)

    // T H E N
    probe.expectMessage(State(toDo, opened = true))

  }

  it should "close" in {

    // G I V E N
    val toDo = ToDo("ToDo1", "ToDo with id 1", new Date().toInstant.toEpochMilli)
    val value = spawn(ToDoBehavior(s"ToDo-${toDo.id}"))
    val probe = TestProbe[State]

    // W H E N
    value ! Open(toDo)
    value ! Close
    value ! GetToDo(probe.ref)

    // T H E N
    probe.expectMessage(State(toDo, opened = false))

  }

}

object ToDoBehaviorSpec {
//  def conf: Config = ConfigFactory.parseString(s"""
//    akka.loglevel = DEBUG
//    akka.loggers = [akka.testkit.TestEventListener]
//    akka.persistence.journal.plugin = "inmemory-journal"
//    akka.persistence.snapshot-store.plugin = "inmemory-snapshot-store"
//    akka.actor.allow-java-serialization = on
//    """)

  val conf: Config = ConfigFactory.parseString(s"""
    akka.actor.allow-java-serialization = on
    akka.persistence.journal.leveldb.dir = "target/typed-persistence-${UUID.randomUUID().toString}"
    akka.persistence.journal.plugin = "akka.persistence.journal.leveldb"
    akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
    akka.persistence.snapshot-store.local.dir = "target/typed-persistence-${UUID.randomUUID().toString}"
    """)
}
