package io.github.mvillafuertem.akka.fsm

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import MessageAdapterSpec.Infrastructure.{ Closed, State }
import org.scalatest.flatspec.AnyFlatSpecLike

/**
 * @author
 *   Miguel Villafuerte
 */
final class MessageAdapterSpec extends ScalaTestWithActorTestKit with AnyFlatSpecLike {

  import MessageAdapterSpec._

  behavior of "The interaction patterns docs"

  it should "contain a sample for adapted response" in {

    // G I V E N
    val infra = spawn(Infrastructure.behavior)

    val app   = spawn(Application(infra))
    val probe = TestProbe[State]()

    // W H E N
    // app ! Application.Open("PEPEPE", probe.ref)
    app ! Application.Close("PEPEPE", probe.ref)

    // T H E N
    probe.expectMessage(Closed("PEPEPE"))
  }

}

object MessageAdapterSpec {

  object Infrastructure {

    val behavior: Behaviors.Receive[Command] = Behaviors.receiveMessage[Command] {
      case Open(task, replyTo)  =>
        replyTo ! Opened(task)
        Behaviors.same
      case Close(task, replyTo) =>
        replyTo ! Closed(task)
        Behaviors.same
    }

    sealed trait Command

    final case class Close(task: String, replyTo: ActorRef[State]) extends Command

    final case class Open(task: String, replyTo: ActorRef[State]) extends Command

    sealed trait State

    final case object Uninitialized extends State

    final case class Opened(task: String) extends State

    final case class Closed(task: String) extends State

  }

  final class Application(context: ActorContext[Application.Command], backend: ActorRef[Infrastructure.Command]) {

    import Application._

    def behavior(inProgress: Set[ActorRef[State]], ref: ActorRef[Infrastructure.State]): Behavior[Application.Command] =
      Behaviors.receiveMessage[Application.Command] {

        case o: Open =>
          context.log.info(s"${o} ~> {}: {}")
          backend ! Infrastructure.Open(o.task, ref)
          behavior(inProgress + o.replyTo, ref)

        case c: Close =>
          context.log.info(s"${c} ~> {}: {}")
          backend ! Infrastructure.Close(c.task, ref)
          behavior(inProgress + c.replyTo, ref)

        case adapter: InfrastructureStateAdapter =>
          adapter.state match {
            case a: Infrastructure.Opened =>
              context.log.info(s"${adapter.state} ~> {}: {}")
              inProgress.foreach(b => b ! a)
              inProgress.foreach { b =>
                context.log.info(s"Tell ${a} ~> {}: {}")
                b ! a
              }
              behavior(inProgress, ref)
            case a: Infrastructure.Closed =>
              context.log.info(s"${adapter.state} ~> {}: {}")
              inProgress.foreach { b =>
                context.log.info(s"Tell ${a} ~> {}: {}")
                b ! a
              }
              behavior(inProgress, ref)
          }
      }

  }

  object Application {

    def apply(backend: ActorRef[Infrastructure.Command]): Behavior[Application.Command] =
      Behaviors.setup[Application.Command] { context: ActorContext[Application.Command] =>
        val adapter: ActorRef[Infrastructure.State] = context.messageAdapter(InfrastructureStateAdapter)
        new Application(context, backend).behavior(Set.empty, adapter)

      }

    sealed trait Command

    final case class Open(task: String, replyTo: ActorRef[State])  extends Command
    final case class Close(task: String, replyTo: ActorRef[State]) extends Command

    private final case class InfrastructureStateAdapter(state: Infrastructure.State) extends Command
  }

}
