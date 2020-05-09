package io.github.mvillafuertem.akka.fsm

import java.net.URI

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import org.scalatest.flatspec.AnyFlatSpecLike

class InteractionPatternsSpec extends ScalaTestWithActorTestKit with AnyFlatSpecLike {

  import InteractionPatternsSpec._

  behavior of "The interaction patterns docs"

  it should "contain a sample for adapted response" in {

    // G I V E N
    val backend = spawn(Behaviors.receiveMessage[Backend.Request] {
      case Backend.StartTranslationJob(taskId, site @ _, replyTo) =>
        replyTo ! Backend.JobStarted(taskId)
        replyTo ! Backend.JobProgress(taskId, 0.25)
        replyTo ! Backend.JobProgress(taskId, 0.50)
        replyTo ! Backend.JobProgress(taskId, 0.75)
        replyTo ! Backend.JobCompleted(taskId, new URI("https://akka.io/docs/sv/"))
        Behaviors.same
    })

    val frontend = spawn(Frontend.translator(backend))
    val probe    = TestProbe[URI]()

    // W H E N
    frontend ! Frontend.Translate(new URI("https://akka.io/docs/"), probe.ref)

    // T H E N
    probe.expectMessage(new URI("https://akka.io/docs/sv/"))
  }

}

object InteractionPatternsSpec {

  // #adapted-response

  object Backend {

    sealed trait Request

    final case class StartTranslationJob(taskId: Int, site: URI, replyTo: ActorRef[Response]) extends Request

    sealed trait Response

    final case class JobStarted(taskId: Int) extends Response

    final case class JobProgress(taskId: Int, progress: Double) extends Response

    final case class JobCompleted(taskId: Int, result: URI) extends Response

  }

  object Frontend {

    sealed trait Command

    final case class Translate(site: URI, replyTo: ActorRef[URI]) extends Command

    private final case class WrappedBackendResponse(response: Backend.Response) extends Command

    def translator(backend: ActorRef[Backend.Request]): Behavior[Command] =
      Behaviors.setup[Command] { context =>
        val backendResponseMapper: ActorRef[Backend.Response] =
          context.messageAdapter(rsp => WrappedBackendResponse(rsp))

        def active(inProgress: Map[Int, ActorRef[URI]], count: Int): Behavior[Command] =
          Behaviors.receiveMessage[Command] {
            case Translate(site, replyTo) =>
              val taskId = count + 1
              backend ! Backend.StartTranslationJob(taskId, site, backendResponseMapper)
              active(inProgress.updated(taskId, replyTo), taskId)

            case wrapped: WrappedBackendResponse =>
              wrapped.response match {
                case Backend.JobStarted(taskId)            =>
                  context.log.info(s"${wrapped.response} ~> Started {}", taskId)
                  Behaviors.same
                case Backend.JobProgress(taskId, progress) =>
                  context.log.info(s"${wrapped.response} ~> Progress {}: {}", taskId, progress)
                  Behaviors.same
                case Backend.JobCompleted(taskId, result)  =>
                  context.log.info(s"${wrapped.response} ~> Completed {}: {}", taskId, result)
                  inProgress(taskId) ! result
                  active(inProgress - taskId, count)
              }
          }

        active(inProgress = Map.empty, count = 0)
      }
  }

  // #adapted-response

}
