package com.codiply.barrio.neighbors.forests

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.Actor.Receive
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.NeighborAggregatorActor
import com.codiply.barrio.neighbors.Point
import com.codiply.barrio.neighbors.Point._

class NeighborhoodForestActor(
    points: List[Point],
    distance: DistanceMetric,
    nTrees: Int,
    aggregatorTimeout: FiniteDuration) extends Actor {
  
  val trees = (1 to nTrees).map(i => 
    context.actorOf(NeighborhoodTreeActor.props(points, distance), "tree-" + i)).toList
  
  def receive: Receive = {
    case request @ GetNeighborsRequest(coordinates, k) => {
      val originalSender = sender
      val aggregator = context.actorOf(NeighborAggregatorActor.props(
          coordinates, k, distance, originalSender, nTrees, aggregatorTimeout))
      trees.foreach(_.tell(request, aggregator))
    }
  }
}