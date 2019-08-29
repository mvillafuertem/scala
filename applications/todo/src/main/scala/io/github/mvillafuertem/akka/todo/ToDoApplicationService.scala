package io.github.mvillafuertem.akka.todo

import java.util.Date

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.{ActorMaterializer, OverflowStrategy}
import io.github.mvillafuertem.akka.todo.configuration.ToDoConfiguration
import io.github.mvillafuertem.akka.todo.domain.ToDo
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoBehavior
import io.github.mvillafuertem.akka.todo.infrastructure.ToDoBehavior.{Close, Command, Open, State}

object ToDoApplicationService extends ToDoConfiguration with App {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  private val value: ActorRef[Command] = actorSystem.spawnAnonymous(ToDoBehavior("id"))

  val toDo = ToDo("ToDo1", "ToDo with id 1", new Date().toInstant.toEpochMilli)

  value ! Open(toDo)
  value ! Open(toDo)
  value ! Open(toDo)
  value ! Open(toDo)
  value ! Open(toDo)
  value ! Open(toDo)

  Thread.sleep(5000)

  PersistenceQuery(actorSystem).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
    .currentPersistenceIds()
//    .currentEventsByPersistenceId("id",0, Long.MaxValue)
//    .map(_.event)
    .runForeach(a => {
        actorSystem.log.info(s"PEPE ~ $a")
//      if (a.opened) {
//        ActorSink.actorRef(value, Close, throw new RuntimeException("error"))
//      } else {
//        actorSystem.log.info(s"PIPO ~ $a")
//        ActorSink.actorRef(value, Open(toDo), throw new RuntimeException("error"))
//      }
    })


//  implicit val actorSystem: ActorSystem[Command] = ActorSystem[Command](
//    ToDoBehavior("id").,
//    "ToDoServiceTypedSystem")

}