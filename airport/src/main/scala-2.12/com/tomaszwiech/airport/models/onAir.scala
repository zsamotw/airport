package com.tomaszwiech.airport.models

import akka.actor.{Actor, ActorRef, Props}
import java.lang.Thread._

object Airplane {
  def props(dest: ActorRef, name: String) = Props(new Airplane(dest, name))

  case class LandingRequest(airplane: Airplane)
  case class StartingRequest(airplane: Airplane)
  case object Landed
}

class Airplane(val dest: ActorRef, val name: String) extends Actor {
  import Airplane.{LandingRequest, StartingRequest, Landed}
  import Steward.{CheckSeatBealts}
  import com.tomaszwiech.airport.models.Airport._
  import com.tomaszwiech.airport.models.WatchTower.{LandingConsent, SecondRing}

  val steward = context.actorOf(Steward.props)

  override def preStart(): Unit = {
    dest ! LandingRequest(this)
  }

  override def receive = {
    case LandingConsent =>
      steward ! CheckSeatBealts(this.name)
      if (secondRing.contener contains self) secondRing.out(self)
      landingLine.in(self)
      for (i <- 5 to 1 by -1) {
        println(s"$this is on landing line. $i seconds to ground")
        sleep(1000)
      }
      landingLine.out(self)
      parking.in(self)
      println(s"The plane $this on the ground. Over")
      sender() ! Landed
    case SecondRing =>
      secondRing.in(self)
      println(s"$this on second ring. Waiting. Over")
  }

  override def toString: String = name
}

object Steward {
  def props = Props(new Steward())
  case class CheckSeatBealts(val airplaneName: String)
}

class Steward extends Actor {
  import Steward.{CheckSeatBealts}

  override def receive = {
    case CheckSeatBealts(airplaneName) =>  println(s"Steward from ${airplaneName} is checking seatbelts")
  }
}
