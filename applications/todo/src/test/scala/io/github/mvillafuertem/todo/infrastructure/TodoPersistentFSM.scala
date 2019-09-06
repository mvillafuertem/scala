package io.github.mvillafuertem.todo.infrastructure

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.receptionist.Receptionist.{Find, Register}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import akka.persistence.typed.{DeleteSnapshotsFailed, PersistenceId, SnapshotFailed}
import com.typesafe.config.{Config, ConfigFactory}
import TodoPersistentFSM.EventSeedBehavior
import TodoPersistentFSM.EventSeedBehavior._
import org.scalatest.{FlatSpecLike, OneInstancePerTest}

final class TodoPersistentFSM extends ScalaTestWithActorTestKit(TodoPersistentFSM.conf)
  with FlatSpecLike
  with OneInstancePerTest {


  val pidCounter = new AtomicInteger(0)

  private def nextPid(): PersistenceId = PersistenceId(s"alert${pidCounter.incrementAndGet()})")


  it should "process an alert" in {

    // G I V E N
    val eventSeed = EventSeed("12", "12")

    val alertSeedBehavior = EventSeedBehavior(nextPid())
    val c = spawn(alertSeedBehavior)
    val probe = TestProbe[EventSeedState]

    // W H E N
    c ! Check(eventSeed)
    c ! GetValue(probe.ref)

    // T H E N
    probe.expectMessage(
      AlertInProgressState(eventSeed, Vector(eventSeed)))

  }

  it should "process an alert with receptionist" in {

    // G I V E N
    val eventSeed = EventSeed("12", "12")

    //val alertSeedBehavior = EventSeedBehavior(nextPid())
    //val c = spawn(alertSeedBehavior)
    val probe = TestProbe[EventSeedState]

    val value = spawn(receptionist("1"))

    Thread.sleep(2000)
    val value2 = spawn(receptionist("1"))

    // W H E N
    value ! Check(eventSeed)
    value2 ! GetValue(probe.ref)

    // T H E N
    probe.expectMessage(
      AlertInProgressState(eventSeed, Vector(eventSeed)))

  }

}

object TodoPersistentFSM {

  def conf: Config = ConfigFactory.parseString(
    s"""
    akka.loglevel = DEBUG
    akka.loggers = [akka.testkit.TestEventListener]
    #
    # akka.persistence.typed.log-stashing = on
    # akka.persistence.journal.leveldb.dir = "target/typed-persistence-${UUID.randomUUID().toString}"
    # akka.persistence.journal.plugin = "akka.persistence.journal.leveldb"
    # akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
    # akka.persistence.snapshot-store.local.dir = "target/typed-persistence-${UUID.randomUUID().toString}"

    akka.actor.allow-java-serialization = on
    akka.persistence.journal.plugin = "inmemory-journal"
    akka.persistence.snapshot-store.plugin = "inmemory-snapshot-store"
    """)

  object EventSeedBehavior {


//    def adapter(replyTo: ActorRef[EventSeedState]): ActorRef[Receptionist.Listing] =
//      Behaviors.messageAdapter[Receptionist.Listing] {
//        case key.Listing(a) =>
//          a.foreach { b => b ! Send(EventSeed("", ""))}
//          Behaviors.same
//      }


    def receptionist(id: String): Behavior[EventSeedCommand] = Behaviors.setup[EventSeedCommand](context => {
      val key: ServiceKey[EventSeedCommand] = ServiceKey(id)
      val receptionist = context.system.receptionist
      val adapter = context.messageAdapter[Receptionist.Listing] {
        case key.Listing(ref) if (ref.nonEmpty) =>
          context.log.info("REF NON EMPTY")
          GetValues(ref)
        case key.Listing(ref) if (ref.isEmpty) =>
          context.log.info("REF EMPTY")
          val value = context.spawnAnonymous(apply(PersistenceId(id)))
          receptionist ! Register(key, value)
          value ! Check(EventSeed("", ""))
          GetValues(ref)
      }
      receptionist ! Find(key, adapter)
      apply(PersistenceId(id))
    })

    def apply(persistenceId: PersistenceId): Behavior[EventSeedCommand] =
      Behaviors.setup[EventSeedCommand](context =>
        new EventSeedBehavior(context, persistenceId).value
      )


    // M O D E L
    final case class EventSeed(id: String, name: String)

    // C O M M A N D
    sealed trait EventSeedCommand

    final case class Check(event: EventSeed) extends EventSeedCommand

    final case class InProgress(event: EventSeed) extends EventSeedCommand

    final case class Send(event: EventSeed) extends EventSeedCommand

    final case class GetValue(replyTo: ActorRef[EventSeedState]) extends EventSeedCommand

    final case class GetValues(set: Set[ActorRef[EventSeedCommand]]) extends EventSeedCommand



    // E V E N T
    sealed trait EventSeedEvent

    final case object EventSeedIdle extends EventSeedEvent

    final case class EventSeedChecked(value: EventSeed) extends EventSeedEvent

    final case class EventSeedInProgress(value: EventSeed) extends EventSeedEvent

    final case class EventSeedSent(value: EventSeed) extends EventSeedEvent


    // S T A T E
    sealed trait EventSeedState {

      val value: EventSeed

      val history: Vector[EventSeed]

    }

    final case class AlertReceivedState(value: EventSeed, history: Vector[EventSeed]) extends EventSeedState

    final case class AlertInProgressState(value: EventSeed, history: Vector[EventSeed]) extends EventSeedState

    final case class AlertSentState(value: EventSeed, history: Vector[EventSeed]) extends EventSeedState

  }

  final class EventSeedBehavior(context: ActorContext[EventSeedCommand], persistenceId: PersistenceId) {


    val value: EventSourcedBehavior[EventSeedCommand, EventSeedEvent, EventSeedState] =
      EventSourcedBehavior[EventSeedCommand, EventSeedEvent, EventSeedState](
        persistenceId,
        emptyState = AlertReceivedState(null, Vector.empty[EventSeed]),
        commandHandler = commandHandler(context),
        eventHandler = eventHandler())
        .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))
        .receiveSignal { // optionally respond to signals
          case (state, _: SnapshotFailed)        => // react to failure
          case (state, _: DeleteSnapshotsFailed) => // react to failure
        }

    private def commandHandler(context: ActorContext[EventSeedCommand]): (EventSeedState, EventSeedCommand) => Effect[EventSeedEvent, EventSeedState] = {
      (state, cmd) =>
        cmd match {

          case Check(alert) =>
            context.log.info(s"check $alert")
            Effect.persist(EventSeedChecked(alert))


          case InProgress(alert) =>
            context.log.info(s"Alerta en progreso $alert")
            Effect.persist(EventSeedInProgress(alert))

          case Send(alert) =>
            context.log.info(s"Alerta notificada a cliente $alert")
            Effect.persist(EventSeedSent(alert))

          case GetValue(replyTo) =>
            replyTo ! state
            Effect.none

          case GetValues(replyTo) =>
            context.log.info(s"Alerta notificada a cliente $replyTo")
            Effect.none
        }
    }

    private def eventHandler(): (EventSeedState, EventSeedEvent) => EventSeedState = {

      (state, event) =>
        state match {

          case AlertReceivedState(_, _) =>
            event match {
              // Se verifica si se puede pasar al estado de in-progress
              case EventSeedChecked(delta) =>
                AlertInProgressState(delta, state.history :+ delta)
              case _ => throw new IllegalStateException(s"unexpected event [$event] in state [$state]")
            }

          case AlertInProgressState(_, _) =>
            event match {
              // Se verifica las ocurrencias de esa misma alerta
              case EventSeedChecked(delta) =>
                if (state.history.size >= 2) {
                  AlertSentState(delta, state.history :+ delta)
                } else {
                  AlertInProgressState(delta, state.history :+ delta)
                }

              case _ => throw new IllegalStateException(s"unexpected event [$event] in state [$state]")
            }

          // Debe crear otra alerta
          case _: AlertSentState =>
            throw new IllegalStateException(s"unexpected event [$event] in state [$state]")
        }
    }

  }


}
