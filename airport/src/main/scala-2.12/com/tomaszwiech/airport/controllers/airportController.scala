package com.tomaszwiech.airport.controllers

import akka.actor.{ActorRef, ActorSystem}
import com.tomaszwiech.airport.models._

object AirportStart extends App {
  val sky = ActorSystem("Sky")
  try {
    val watchTower: ActorRef = sky.actorOf(WatchTower.props)
    val plain01: ActorRef = sky.actorOf(Airplane.props(watchTower, "Cesna", 2000))
    Thread.sleep(2000)
    val plain02: ActorRef = sky.actorOf(Airplane.props(watchTower, "Boeing", 1000))
    Thread.sleep(2000)
    val plain03: ActorRef = sky.actorOf(Airplane.props(watchTower, "Helicopter", 1000))
    Thread.sleep(2000)
    val plain04: ActorRef = sky.actorOf(Airplane.props(watchTower, "FlyFly", 1000))
  } catch {
    case e: Exception => println(e)
  } finally {
    Thread.sleep(50000)
    Airport.parking.printContent
    Airport.secondRing.printContent
    sky.terminate
  }
}


