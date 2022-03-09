package io.github.mvillafuertem.akka.fsm.configuration

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.{ actor, Done }
import org.slf4j.LoggerFactory

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success }

/**
 * @author
 *   Miguel Villafuerte
 */
final class ToDoApplicationConfiguration(context: ActorContext[Done]) extends ToDoConfiguration {

  private val log = LoggerFactory.getLogger(getClass)

  // override lazy val log: Logger = context.system.log
  implicit lazy val untypedSystem: actor.ActorSystem          = context.system.toClassic
  implicit lazy val materializer: Materializer                = Materializer(untypedSystem)
  implicit lazy val contextExecutor: ExecutionContextExecutor = context.system.executionContext

  val serverBinding: Future[Http.ServerBinding] =
    Http()
      .newServerAt(toDoConfigurationProperties.interface, toDoConfigurationProperties.port)
      .bind(toDoAPI.routes)

  serverBinding.onComplete {
    case Success(bound) =>
      log.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/docs")
    case Failure(e)     =>
      log.error(s"Server could not start!")
      e.printStackTrace()
      context.self ! Done
  }

  private val behavior: Behaviors.Receive[Done] = Behaviors.receiveMessage { case Done =>
    log.error(s"Server could not start!")
    Behaviors.stopped
  }

}

object ToDoApplicationConfiguration {

  def apply(): Behavior[Done] = Behaviors.setup[Done](context => new ToDoApplicationConfiguration(context).behavior)

}
