package io.github.mvillafuertem.alpakka.sns

import akka.actor.Scheduler
import akka.pattern.after

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}
import scala.util.Random

// https://gist.github.com/viktorklang/9414163
object RetryDelays {

  def retry[T](f: => Future[T], delay: Seq[FiniteDuration], retries: Int, defaultDelay: FiniteDuration)(implicit ec: ExecutionContext, s: Scheduler): Future[T] = {
    f recoverWith {
      case _ if retries > 0 =>
        after(delay.headOption.getOrElse(defaultDelay), s)(retry(f, delay.tail, retries - 1, defaultDelay))
    }
  }

  def withDefault(delays: List[FiniteDuration], retries: Int, default: FiniteDuration): Seq[FiniteDuration] =
    if (delays.length > retries) delays take retries
    else delays ++ List.fill(retries - delays.length)(default)

  def withJitter(delays: Seq[FiniteDuration], maxJitter: Double, minJitter: Double): Seq[Duration] =
    delays.map(_ * (minJitter + (maxJitter - minJitter) * Random.nextDouble))

  val fibonacci: LazyList[FiniteDuration] = 0.seconds #:: 1.seconds #:: (fibonacci zip fibonacci.tail).map(t => t._1 + t._2)
}
