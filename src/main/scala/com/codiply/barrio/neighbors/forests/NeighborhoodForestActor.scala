package com.codiply.barrio.neighbors.forests

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.Props
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.NeighborAggregatorActor
import com.codiply.barrio.neighbors.Point
import com.codiply.barrio.neighbors.Point._
import com.codiply.barrio.neighbors.NodeStats

object NeighborhoodForestActor {
  def props(
    points: List[Point],
    distance: DistanceMetric,
    nTrees: Int,
    aggregatorTimeout: FiniteDuration) = 
      Props(new NeighborhoodForestActor(points, distance, nTrees, aggregatorTimeout))
}

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
    case GetNodeStatsRequest => {
      val runtime = Runtime.getRuntime

      val mb = 1024 * 1024
      val freeMemoryMB = runtime.freeMemory.toDouble / mb;
      val totalMemoryMB = runtime.totalMemory.toDouble / mb;
      val maxMemoryMB = runtime.maxMemory.toDouble /mb;
      
      sender ! GetNodeStatsResponse(NodeStats(
          freeMemoryMB = freeMemoryMB, 
          totalMemoryMB = totalMemoryMB,
          maxMemoryMB = maxMemoryMB, 
          usedMemoryMB = totalMemoryMB - freeMemoryMB
      ))
    }
  }
}