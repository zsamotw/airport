package com.tomaszwiech.airport.controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import com.tomaszwiech.airport.models.Airplane.Ask
import com.tomaszwiech.airport.models._

object AirportStart extends App {
  val sky = ActorSystem("Sky")
  try {
    val watchTower: ActorRef = sky.actorOf(WatchTower.props)
    val plain01: ActorRef = sky.actorOf(Airplane.props(watchTower, "Cesna"))
    val plain02: ActorRef = sky.actorOf(Airplane.props(watchTower, "Boeing"))
    val plain03: ActorRef = sky.actorOf(Airplane.props(watchTower, "Helicopter"))
    val plain04: ActorRef = sky.actorOf(Airplane.props(watchTower, "FlyFly"))
    plain01 ! Ask
    Thread.sleep(2000)
    plain02 ! Ask
    Thread.sleep(2000)
    plain03 ! Ask
    Thread.sleep(2000)
    plain04 ! Ask
  } catch {
    case e: Exception => println(e)
  } finally {
     Thread.sleep(27000)
    Airport.parking.printContent
    Airport.secondRing.printContent
    sky.terminate
  }
} 
