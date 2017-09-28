package com.tomaszwiech.airport.models

import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.ListBuffer

object WatchTower {
  def props = Props(new WatchTower)

  case object LandingPermission
  case object StartingPermission
  case object SecondRing
}

class  WatchTower extends Actor {
  import com.tomaszwiech.airport.models.Airplane.{LandingRequest, StartingRequest, Landed, Started}
  import com.tomaszwiech.airport.models.Airport._
  import WatchTower.{LandingPermission, StartingPermission, SecondRing}

  case class DecissionData(isEmptySecondRing: Boolean, enoughPlacesLine: Boolean, enoughPlacesParking: Boolean, enoughPlacesSecondRing: Boolean, isAnyStartingRequest: Boolean)

  override def preStart(): Unit = {
    println("WatchTower is ready!")
  }

  override def postStop(): Unit = {
    println("WatchTower shutdown. Airplanes take care!")
  }

  def receive = {
    case LandingRequest(airplane) =>
      val decisionData = DecissionData(emptySecondRing, areEnoughPlaces(landingLine), areEnoughPlaces(parking), areEnoughPlaces(secondRing), noStartingRequests)
      decisionData match {
        case DecissionData(true, true, true, _, true) =>
          sender() ! LandingPermission
          println (s"Permission for landing ${airplane.name} Over!!")
        case DecissionData(_, _, true, true, _) =>
          println(s"${airplane.name} stay on Second Ring. Over")
          sender() ! SecondRing
        case DecissionData(_, _, false,false, _) => println ("BingBang no place around. Run away!!!!")
        case _ =>
          println (s"Untypical situation!!!")
      }
    case Landed =>
      if(noStartingRequests && !emptySecondRing) {
        println(s"Landing Line is empty. No airplanes preparing to start. ${secondRing.head} start landing.")
        secondRing.head ! LandingPermission
      }
      else if(!noStartingRequests){
        println(s"${startingRequests.head} you can take off.")
        startingRequests.head ! StartingPermission
      }
      else println("No airplanes in second ring and there are no starting requests")
    case StartingRequest(airplane) =>
      if(areEnoughPlaces(landingLine) && isHeadOfStartingRequests(sender)) {
        println(s"${airplane.name} can take off!!!")
        sender() ! StartingPermission
      } else {
        println(s"${airplane.name}, you have to wait in queue. ${startingRequests.contener.length} airplanes waiting for permission")
      }
    case Started =>
      if(noStartingRequests && !emptySecondRing) {
        println(s"Landing Line is empty. No airplanes preparing to start. ${secondRing.head} start landing.")
        secondRing.head ! LandingPermission
      }
      else if(!noStartingRequests){
        println(s"${startingRequests.head} you can take off.")
        startingRequests.head ! StartingPermission
      }
      else println("No airplanes in second ring and there are no starting requests")
  }

  def areEnoughPlaces(area: Area): Boolean = area.areEnoughPlaces

  def emptySecondRing: Boolean = secondRing.contener.isEmpty

  def noStartingRequests: Boolean = startingRequests.contener.isEmpty

  def isHeadOfStartingRequests(airplane: ActorRef) = startingRequests.contener.head == airplane
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
  var startingRequests = new Area("Starting Requests", 5, new ListBuffer[ActorRef])
  var landingLine = new Area("Landing Line", 1, new ListBuffer[ActorRef])
  var parking = new Area("Parking", 5, new ListBuffer[ActorRef])
  var secondRing = new Area("Second Ring", 5, new ListBuffer[ActorRef])
}
