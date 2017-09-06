package com.tomaszwiech.airport.models

import akka.actor.{Actor, ActorRef, Props}
import java.lang.Thread._

object Airplane {
  case object LandingConsent
  case object SecondRing
  case object Ask

  def props(dest: ActorRef, name: String) = Props(new Airplane(dest, name))
}

class Airplane(dest: ActorRef, val name: String) extends Actor {
  import com.tomaszwiech.airport.models.Airplane.{Ask, LandingConsent, SecondRing}
  import com.tomaszwiech.airport.models.WatchTower.{LandingRequest}
  import com.tomaszwiech.airport.models.Airport._

  var isLendingConsent = false

  def receive = {
    case LandingConsent =>
      if (isLendingConsent) {
        println("I have just started landing. Over")
      }
      else {
        isLendingConsent = true
        if (secondRing.contener contains self) secondRing.contener -= self
        landingLine.contener += self
        for (i <- 5 to 1 by -1) {
          println(s"$this is on landing line. $i seconds to ground")
          sleep(1000)
        }
        landingLine.contener -= self
        parking.contener += self
        println(s"The plane $this on the ground. Over")
      }
    case SecondRing =>
      if(secondRing.contener contains self) {
        println(s"Ok. $this is staying on Second Ring")
      }
      else {
        secondRing.contener += self
        println(s"$this on second ring. Waiting. Over")
      }
      sleep(5000)
      dest ! LandingRequest(this)
      println(s"$this on Second Ring. Sending new request for landing")
    case Ask =>
      println(s"$this asking WatchTower for consent of landing")
      dest ! LandingRequest(this)
  }

  override def toString: String = name
}
