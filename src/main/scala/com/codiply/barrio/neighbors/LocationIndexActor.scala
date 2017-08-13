package com.codiply.barrio.neighbors

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.Props

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates

object LocationIndexActorProtocol {
  final case class GetLocationRequest(id: String)
  final case class GetLocationResponse(location: Option[Coordinates])
}

object LocationIndexActor {
  def props(points: List[Point]): Props = Props(new LocationIndexActor(points))
}

class LocationIndexActor(points: List[Point]) extends Actor {
  import LocationIndexActorProtocol._

  val idToLocation: Map[String, Coordinates] = points.map(p => (p.id, p.location)).toMap

  def receive: Receive = {
    case GetLocationRequest(id) => {
      val location = idToLocation.get(id)
      sender ! GetLocationResponse(location)
    }
  }
}
