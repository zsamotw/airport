package com.tomaszwiech.airport.controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import com.tomaszwiech.airport.models._

object AirportStart extends App {
  val sky = ActorSystem("Sky")
  try {
    val watchTower: ActorRef = sky.actorOf(WatchTower.props)
    val plain01: ActorRef = sky.actorOf(Airplane.props(watchTower, "Cesna"))
    Thread.sleep(2000)
    val plain02: ActorRef = sky.actorOf(Airplane.props(watchTower, "Boeing"))
    Thread.sleep(2000)
    val plain03: ActorRef = sky.actorOf(Airplane.props(watchTower, "Helicopter"))
    Thread.sleep(2000)
    val plain04: ActorRef = sky.actorOf(Airplane.props(watchTower, "FlyFly"))
  } catch {
    case e: Exception => println(e)
  } finally {
    Thread.sleep(27000)
    Airport.parking.printContent
    Airport.secondRing.printContent
    sky.terminate
  }
} 
