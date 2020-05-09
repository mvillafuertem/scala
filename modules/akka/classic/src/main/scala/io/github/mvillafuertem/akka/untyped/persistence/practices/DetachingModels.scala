package io.github.mvillafuertem.akka.untyped.persistence.practices

import akka.actor.{ ActorLogging, ActorSystem, Props }
import akka.persistence.PersistentActor
import akka.persistence.journal.{ EventAdapter, EventSeq }
import com.typesafe.config.ConfigFactory
import io.github.mvillafuertem.akka.untyped.persistence.practices.DomainModel.{ ApplyCoupon, Coupon, User }

import scala.collection.mutable

object DetachingModels extends App {

  class CouponManager extends PersistentActor with ActorLogging {

    import DomainModel._

    val coupons: mutable.Map[String, User] = mutable.Map[String, User]()

    override def persistenceId: String = "coupon-manager"

    override def receiveCommand: Receive = {

      case ApplyCoupon(coupon, user) =>
        if (!coupons.contains(coupon.code))
          persist(CouponApplied(coupon.code, user)) { e =>
            log.info(s"Persisted $e")
            coupons.put(coupon.code, user)
          }

    }

    override def receiveRecover: Receive = {
      case event @ CouponApplied(code, user) =>
        log.info(s"Recovered $event")
        coupons.put(code, user)
    }
  }

  val actorSystem   = ActorSystem("DetachingModels", ConfigFactory.load().getConfig("detachingModels"))
  val couponManager = actorSystem.actorOf(Props[CouponManager], "couponManager")

  for (i <- 1 to 5) {
    val coupon = Coupon(s"MEGA_COUPON_$i", 100)
    val user   = User(s"$i", s"user_$i@email.com")

    couponManager ! ApplyCoupon(coupon, user)
  }

}

object DomainModel {

  case class User(id: String, email: String)
  case class Coupon(code: String, promotionAmount: Int)

  case class ApplyCoupon(coupon: Coupon, user: User)
  case class CouponApplied(code: String, user: User)
}

object DataModel {
  case class WrittenCouponApplied(code: String, userId: String, userEmail: String)
}

class ModelAdapter extends EventAdapter {
  import DataModel._
  import DomainModel._

  override def manifest(event: Any): String = "CMA"

  // Journal -> Serializer -> fromJournal -> to the Actor
  override def fromJournal(event: Any, manifest: String): EventSeq =
    event match {
      case event @ WrittenCouponApplied(code, userId, userEmail) =>
        println(s"Converting $event to DomainModel")
        EventSeq.single(CouponApplied(code, User(userId, userEmail)))
      case other                                                 => EventSeq.single(other)
    }

  // Actor -> toJournal -> Serializer -> Journal
  override def toJournal(event: Any): Any                          =
    event match {
      case event @ CouponApplied(code, user) =>
        println(s"Converting $event to DataModel")
        WrittenCouponApplied(code, user.id, user.email)
    }
}
