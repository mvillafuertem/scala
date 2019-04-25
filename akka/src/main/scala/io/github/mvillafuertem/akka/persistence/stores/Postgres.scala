package io.github.mvillafuertem.akka.persistence.stores
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Postgres extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("Postgres", ConfigFactory.load().getConfig("postgres"))
  val simplePersistentActor = actorSystem.actorOf(Props[SimplePersistentActor], "simplePersistentActor")

  for (i <- 1 to 10) {
    simplePersistentActor ! s""
  }

}
