package com.codiply.barrio.neighbors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import Point._

object NeighborhoodPatchActor {
  def props(points: List[Point], distance: DistanceMetric) = Props(new NeighborhoodPatchActor(points, distance))
}

object NeighborhoodPatchActorProtocol {
  final case class GetNeighborsRequest(coordinates: List[Double], k: Int)
  final case class GetNeighborsResponse(neighbors: List[Point])
}

class NeighborhoodPatchActor(
    points: List[Point],
    distance: DistanceMetric) extends Actor {
  import NeighborhoodPatchActorProtocol._
  
  def receive: Receive = {
    case GetNeighborsRequest(coordinates, k) => 
      val neighbors = points.sortBy(p => distance(p.coordinates, coordinates)).take(k)
      sender ! GetNeighborsResponse(neighbors)
  }
}