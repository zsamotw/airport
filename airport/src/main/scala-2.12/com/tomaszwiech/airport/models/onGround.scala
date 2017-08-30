package com.tomaszwiech.airport.models

import akka.actor.Actor
import scala.collection.mutable.ListBuffer

sealed trait AirplaneRequest
case object LandingRequest extends AirplaneRequest

class WatchTower extends Actor {
  def receive = {
    case LandingRequest =>
      val decision = {
        if(Airport.landingLine.isEmpty && Airport.parking.isEmpty) 0
        else if(!Airport.secondRing.isEmpty) 1
        else 2
      }
      decision match {
        case 0 =>
          sender ! LandingConsent
          println(s"Agreement for landing $sender Over!!")
        case 1 => println("BingBang no place")
        case 2 =>
          sender ! SecondRing
          println(s" $sender go to the second ring. Line isn't empty. Over")
      }
  }
}

class Area (val name: String, val max: Int, var contener: ListBuffer[Airplane]) {
  def isEmpty = contener.length < max

  def printContent {
    println(s"$name: ")
    contener.foreach(plane => print(s"${plane.name} - "))
    println(s"\n${contener.length} / $max ")
  }
}

case object Airport {
  var landingLine = new Area("Landing Line", 1, new ListBuffer[Airplane] )
  var parking = new Area("Parking", 5, new ListBuffer[Airplane])
  var secondRing = new Area("Second Ring", 10, new ListBuffer[Airplane])
}
