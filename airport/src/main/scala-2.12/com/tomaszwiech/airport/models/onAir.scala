package com.tomaszwiech.airport.models

import akka.actor.{Actor, ActorRef}
import java.lang.Thread._

sealed trait WatchTowerComunicate

case object LandingConsent extends WatchTowerComunicate
case object SecondRing extends WatchTowerComunicate
case object Ask extends WatchTowerComunicate

class Airplane(dest: ActorRef, val name: String) extends Actor {
  def receive = {
    case LandingConsent =>
      Airport.landingLine.contener += this
      for (i <- 1 to 5) {
        println(s"$this is on landing line in $i")
        sleep(1000)
      }
      Airport.landingLine.contener -= this
      Airport.parking.contener += this
      println(s"The plane $this on the ground. Over")
    case SecondRing =>
      println(s"$this on the way to Second Ring")
      sleep(1000)
      Airport.secondRing.contener += this
      println(s"$this on second ring. Waiting. Over")
      sleep(5000)
      dest ! LandingRequest
      Airport.secondRing.contener -= this
      println(s"$this out of Second Ring. Sending new request for landing")
    case Ask =>
      println(s"$this asking for consent of landing")
      dest ! LandingRequest
  }

  override def toString: String = name
}
