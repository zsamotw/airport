package com.tomaszwiech.airport.models

import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.ListBuffer

object WatchTower {
  case class LandingRequest(airplane: Airplane)

  def props = Props(new WatchTower)
}

class  WatchTower extends Actor {
  import com.tomaszwiech.airport.models.Airplane.{LandingConsent, SecondRing}
  import com.tomaszwiech.airport.models.WatchTower.{LandingRequest}
  import com.tomaszwiech.airport.models.Airport._

  case class DecissionData(isEmptySecondRing: Boolean, enoughPlacesLine: Boolean, enoughPlacesParking: Boolean, enoughPlacesSecondRing: Boolean)

  def receive = {
    case LandingRequest(airplane) =>
      val decisionData = DecissionData(emptySecondRing, areEnoughPlaces(landingLine), areEnoughPlaces(parking), areEnoughPlaces(secondRing))
      decisionData match {
        case DecissionData(true, true, true, _) =>
          sender() ! LandingConsent
          println (s"Agreement for landing ${airplane.name} Over!!")
        case DecissionData(false, true, true, _) if sender == secondRing.head =>
          println(s"${airplane.name} you are head of Second Ring and line is empty. You can start landing!!!!")
          sender() ! LandingConsent
        case DecissionData(false, false, true, _) if secondRing.contener contains sender =>
          println (s"${airplane.name}, you aren't head od Second Ring. Stay in queue")
          sender() ! SecondRing
          secondRing.head ! LandingConsent
        case DecissionData(false,false,false,false) => println ("BingBang no place around. Run away!!!!")
        case _ =>
          sender () ! SecondRing
          println (s"${airplane.name} go to the second ring. Line isn't empty. Over")
      }
  }

  def areEnoughPlaces(area: Area): Boolean = area.areEnoughPlaces

  def emptySecondRing: Boolean = secondRing.contener.isEmpty
}

class Area (val name: String, val max: Int, var contener: ListBuffer[ActorRef]) {
  def areEnoughPlaces: Boolean = contener.length < max

  def head: ActorRef = contener.head

  def printContent {
    println(s"$name: ")
    contener.foreach(plane => print(s"$plane - "))
    println(s"\n${contener.length} / $max ")
  }
}

case object Airport {
  var landingLine = new Area("Landing Line", 1, new ListBuffer[ActorRef] )
  var parking = new Area("Parking", 5, new ListBuffer[ActorRef])
  var secondRing = new Area("Second Ring", 5, new ListBuffer[ActorRef])
}
