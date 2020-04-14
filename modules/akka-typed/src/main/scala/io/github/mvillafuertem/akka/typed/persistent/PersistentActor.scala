package io.github.mvillafuertem.akka.typed.persistent

import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, TypedActorContext}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import scala.concurrent.duration._

/**
 * @author Miguel Villafuerte
 */
object PersistentActor {

  // I N I T  M O D E L
  case class Person(firstName: String, lastName: String)
  // E N D  M O D E L

  // I N I T  P R O T O C O L

  // c o m m a n d s
  sealed trait Command
  final case class AddPerson(person: Person) extends Command
  final case class GetPerson(replyTo: ActorRef[Person]) extends Command
  final case class ModifyLastName(newLastName: String) extends Command

  // e v e n t s
  sealed trait Event
  final case class PersonAdded(person: Person) extends Event
  final case class LastNameModified(prevLastName: String, newLastName: String) extends Event

  // s t a t e
  case class PersonState(person: Option[Person] = None) {
    def update(e: Event): PersonState = e match {
      case PersonAdded(person) => PersonState(Some(Person(person.firstName, person.lastName)))
      case LastNameModified(_, newLastName) =>PersonState(this.person.map(p => p.copy(lastName = newLastName)))
      case _ => PersonState(None)
    }
  }
  // E N D  P R O T O C O L


  // C O M M A N D  H A N D L E R
  def commandHandler(context: TypedActorContext[Command], entityId: String) : CommandHandler[Command, Event, PersonState]  = {
    val log = context.asScala.log
    (state, cmd) => cmd match {
      case AddPerson(p) =>
        Effect.persist(PersonAdded(p))
      case ModifyLastName(newLastName) =>
        val prevLastName = state.person.getOrElse(Person("", "")).lastName
        Effect.persist(LastNameModified(prevLastName, newLastName))
          .thenRun(_ => log.info(s"Last name changed from $prevLastName to $newLastName"))
      case GetPerson(replyTo) =>
        replyTo ! state.person.getOrElse(Person("", ""))
        Effect.none
    }
  }

  // E V E N T  H A N D L E R
  def eventHandler: (PersonState, Event) => PersonState = {
    case (state, e @ PersonAdded(_)) => state.update(e)
    case (state, e @ LastNameModified(_, _)) => state.update(e)
  }

  // B E H A V I O R
  def behavior(entityId: String): Behavior[Command] = Behaviors.setup[Command] {
    context =>
    context.log.info(s"Stated entity $entityId")
    EventSourcedBehavior[Command, Event, PersonState](
      persistenceId = PersistenceId.ofUniqueId(entityId),
      emptyState = PersonState(),
      commandHandler = commandHandler(context, entityId),
      eventHandler = eventHandler
    )
  }

  // S U P E R V I S O R  S T R A T E G Y
  val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
    minBackoff = 5 seconds,
    maxBackoff = 20 seconds,
    randomFactor = 0.1
  )

  def supervisedBehavior(entityId: String): Behavior[Command] =
    Behaviors
      .supervise(behavior(entityId))
      .onFailure(supervisorStrategy)

}
