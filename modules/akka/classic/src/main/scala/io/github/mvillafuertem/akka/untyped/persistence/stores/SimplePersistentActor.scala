package io.github.mvillafuertem.akka.untyped.persistence.stores
import akka.actor.ActorLogging
import akka.persistence._

final class SimplePersistentActor extends PersistentActor with ActorLogging {

  override def persistenceId: String = "simple-persistent-actor"

  var nMessages = 0

  override def receiveCommand: Receive = {
    case "print"                       =>
      log.info(s"I have persisted $nMessages sso far")
    case "snap"                        =>
      saveSnapshot(nMessages)
    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Save snapshot was succesful $metadata")
    case SaveSnapshotFailure(_, cause) =>
      log.warning(s"Save snapshot failed $cause")
    case message                       =>
      persist(message) { _ =>
        log.info(s"Persisting $message")
        nMessages += 1
      }
  }

  override def receiveRecover: Receive = {
    case RecoveryCompleted              =>
      log.info("Recovery done")
    case SnapshotOffer(_, payload: Int) =>
      log.info(s"Recovered snapshot $payload")
      nMessages += 1
    case message                        =>
      log.info(s"Recovered $message")
      nMessages += 1
  }

}
