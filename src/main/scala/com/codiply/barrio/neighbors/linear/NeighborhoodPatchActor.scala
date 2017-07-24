package com.codiply.barrio.neighbors.linear

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import com.codiply.barrio.neighbors.Point
import com.codiply.barrio.neighbors.Point._

object NeighborhoodPatchActor {
  def props(points: List[Point], distance: DistanceMetric) = Props(new NeighborhoodPatchActor(points, distance))
}

class NeighborhoodPatchActor(
    points: List[Point],
    distance: DistanceMetric) extends Actor {
  import com.codiply.barrio.neighbors.ActorProtocol._
  
  def receive: Receive = {
    case GetNeighborsRequest(coordinates, k, timeout) => 
      val neighbors = points.sortBy(p => distance(p.coordinates, coordinates)).take(k)
      sender ! GetNeighborsResponse(neighbors)
  }
}
