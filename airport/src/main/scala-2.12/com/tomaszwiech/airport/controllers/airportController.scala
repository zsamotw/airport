package com.tomaszwiech.airport.controllers

import akka.actor.{ActorRef, ActorSystem, Props}
import com.tomaszwiech.airport.models._

object AirportStart extends App {
  val sky = ActorSystem("Sky")
  try {
    val watchTower: ActorRef = sky.actorOf(Props(classOf[WatchTower]))
    val plain01: ActorRef = sky.actorOf(Props(new Airplane(watchTower, "Cesna")))
    val plain02: ActorRef = sky.actorOf(Props(classOf[Airplane], watchTower, "Boeing"))
    val plain03: ActorRef = sky.actorOf(Props(classOf[Airplane], watchTower, "Smug"))
    val plain04: ActorRef = sky.actorOf(Props(classOf[Airplane], watchTower, "FlyFly"))
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
    sky.terminate
    Thread.sleep(20000)
    Airport.parking.printContent
    Airport.secondRing.printContent
  }
} 
