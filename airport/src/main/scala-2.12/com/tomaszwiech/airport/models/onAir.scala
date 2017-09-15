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
  import com.tomaszwiech.airport.models.Airport._
  import com.tomaszwiech.airport.models.WatchTower.{LandingConsent, SecondRing}

  override def preStart(): Unit = {
    dest ! LandingRequest(this)
  }

  override def receive = {
      case LandingConsent =>
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
