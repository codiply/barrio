package com.codiply.barrio.neighbors

import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Actor.Receive
import akka.actor.Props
import Point._

object NeighborhoodNodeActor {
  def props(points: List[Point], distance: DistanceMetric, aggregatorTimeout: FiniteDuration) = 
    Props(new NeighborhoodNodeActor(points, distance, aggregatorTimeout))
}

class NeighborhoodNodeActor(
    points: List[Point],
    distance: DistanceMetric,
    aggregatorTimeout: FiniteDuration) extends Actor {
  import NeighborhoodPatchActorProtocol._
  
  val (points1, points2) = points.splitAt(points.length / 2)
  
  val neighborhoodPatches = 
    Array(points1, points2).zipWithIndex.map { case (ps, i) =>
      context.actorOf(NeighborhoodPatchActor.props(ps, distance), "patch-" + i) }
  
  val numberOfPatches = neighborhoodPatches.length
  
  def receive: Receive = { 
    case request @ GetNeighborsRequest(coordinates, k) => {
      val originalSender = sender
      val aggregator = context.actorOf(NeighborAggregatorActor.props(
          coordinates, k, distance, originalSender, numberOfPatches, aggregatorTimeout))
      neighborhoodPatches.foreach(_.tell(request, aggregator))
    }
  }
}