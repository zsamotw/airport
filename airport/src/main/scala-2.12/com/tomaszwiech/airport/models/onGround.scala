package com.tomaszwiech.airport.models

import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.ListBuffer

object WatchTower {
  def props = Props(new WatchTower)

  case object LandingConsent
  case object SecondRing
}

class  WatchTower extends Actor {
  import com.tomaszwiech.airport.models.Airplane.{LandingRequest, Landed}
  import com.tomaszwiech.airport.models.Airport._
  import WatchTower.{LandingConsent, SecondRing}

  case class DecissionData(isEmptySecondRing: Boolean, enoughPlacesLine: Boolean, enoughPlacesParking: Boolean, enoughPlacesSecondRing: Boolean)

  override def preStart(): Unit = {
    println("WatchTower is ready!")
  }

  override def postStop(): Unit = {
    println("WatchTower shutdown. Airplanes take care!")
  }

  def receive = {
    case LandingRequest(airplane) =>
      val decisionData = DecissionData(emptySecondRing, areEnoughPlaces(landingLine), areEnoughPlaces(parking), areEnoughPlaces(secondRing))
      decisionData match {
        case DecissionData(true, true, true, _) =>
          sender() ! LandingConsent
          println (s"Agreement for landing ${airplane.name} Over!!")
        case DecissionData(_, _, true, true) =>
          println(s"${airplane.name} stay on Second Ring. Over")
          sender() ! SecondRing
        case DecissionData(_, _, false,false) => println ("BingBang no place around. Run away!!!!")
        case _ =>
          println (s"Untypical situation!!!")
      }
    case Landed =>
      if(secondRing.contener.nonEmpty) {
        println(s"Landing Line is empty. ${secondRing.head} start landing.")
        secondRing.head ! LandingConsent
      }
  }

  def areEnoughPlaces(area: Area): Boolean = area.areEnoughPlaces

  def emptySecondRing: Boolean = secondRing.contener.isEmpty
}

class Area (val name: String, val max: Int, var contener: ListBuffer[ActorRef]) {

  def head: ActorRef = contener.head

  def in(ref: ActorRef): Unit =  { contener += ref }

  def out(ref: ActorRef): Unit = { contener -= ref }

  def areEnoughPlaces: Boolean = contener.length < max

  def printContent: Unit = {
    println(s"$name: ")
    contener.foreach(plane => print(s"${plane} - "))
    println(s"\n${contener.length} / $max ")
  }
}

case object Airport {
  var landingLine = new Area("Landing Line", 1, new ListBuffer[ActorRef] )
  var parking = new Area("Parking", 5, new ListBuffer[ActorRef])
  var secondRing = new Area("Second Ring", 5, new ListBuffer[ActorRef])
}
