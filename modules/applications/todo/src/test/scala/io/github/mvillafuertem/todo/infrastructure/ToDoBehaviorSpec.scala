package io.github.mvillafuertem.todo.infrastructure

import java.util.Date

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.github.mvillafuertem.todo.domain.ToDo
import io.github.mvillafuertem.todo.infrastructure.ToDoBehavior.{Close, GetToDo, Open, State}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, OneInstancePerTest}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author Miguel Villafuerte
 */
final class ToDoBehaviorSpec extends ScalaTestWithActorTestKit(ToDoBehaviorSpec.conf)
  with AnyFlatSpecLike
  with Matchers
  with BeforeAndAfterAll
  with OneInstancePerTest {

  override implicit val timeout: Timeout = 10 second

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = Materializer(actorSystem)

  behavior of "ToDo Behavior Spec"

  it should "open" in {

    // G I V E N
    val toDo = ToDo("ToDo", "ToDo with id 1234567890", new Date().toInstant.toEpochMilli)
    val value = spawn(ToDoBehavior(s"ToDo-123"))
    val value1 = spawn(ToDoBehavior(s"ToDo-123"))
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
  def conf: Config = ConfigFactory.parseString(s"""
    akka.loglevel = DEBUG
    akka.loggers = [akka.testkit.TestEventListener]
    akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
    akka.persistence.journal.inmem.test-serialization = on
    akka.actor.allow-java-serialization = on
    """)

//  val conf: Config = ConfigFactory.parseString(s"""
//    akka.actor.allow-java-serialization = on
//    #akka.persistence.journal.leveldb.dir = "target/typed-persistence-${UUID.randomUUID().toString}"
//    akka.persistence.journal.leveldb.dir = "target/typed-persistence"
//    akka.persistence.journal.plugin = "akka.persistence.journal.leveldb"
//    akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
//    #akka.persistence.snapshot-store.local.dir = "target/typed-persistence-${UUID.randomUUID().toString}"
//    akka.persistence.snapshot-store.local.dir = "target/typed-persistence"
//    """)
}
