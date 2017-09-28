package com.tomaszwiech.airport.models

import akka.actor.{Actor, ActorRef, Props}
import java.lang.Thread._

object Airplane {
  def props(dest: ActorRef, name: String, turnOffTime: Int) = Props(new Airplane(dest, name, turnOffTime))

  case class LandingRequest(airplane: Airplane)
  case class StartingRequest(airplane: Airplane)
  case object Landed
  case object Started
}

class Airplane(val dest: ActorRef, val name: String, turnOffTime: Int) extends Actor {
  import Airplane.{LandingRequest, StartingRequest, Landed, Started}
  import Steward.{CheckSeatBealts}
  import com.tomaszwiech.airport.models.Airport._
  import com.tomaszwiech.airport.models.WatchTower.{LandingConsent, StartingConsent, SecondRing}

  val steward = context.actorOf(Steward.props)

  override def toString: String = name

  override def preStart(): Unit = dest ! LandingRequest(this)

  override def postStop(): Unit = println(s"${name} is on the way to other airport. Bye!")

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
      sleep(turnOffTime)
      println(s"$this sending request for taking off")
      sender ! StartingRequest(this)
      startingRequests.in(self)
    case SecondRing =>
      secondRing.in(self)
      println(s"$this on second ring. Waiting. Over")
    case StartingConsent =>
      println(s"$this starts procedure of taking off")
      steward ! CheckSeatBealts(this.name)
      parking.out(self)
      landingLine.in(self)
      for (i <- 5 to 1 by -1) {
        println(s"$this is taking off. $i seconds to be on the sky")
        sleep(1000)
      }
      landingLine.out(self)
      startingRequests.out(self)
      sender() ! Started
      context stop self
  }
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
