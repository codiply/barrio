package com.codiply.barrio.neighbors.forests

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.NeighborAggregatorActor
import com.codiply.barrio.neighbors.Point
import com.codiply.barrio.neighbors.Point._
import com.codiply.barrio.neighbors.NodeStats
import com.codiply.barrio.neighbors.TreeStats

object NeighborhoodForestActor {
  def props(
    points: List[Point],
    distance: DistanceMetric,
    nTrees: Int) = 
      Props(new NeighborhoodForestActor(points, distance, nTrees))
}

class NeighborhoodForestActor(
    points: List[Point],
    distance: DistanceMetric,
    nTrees: Int) extends Actor {
  import NeighborhoodTreeActorProtocol._
  
  val timeout: FiniteDuration = 5 seconds
  implicit val askTimeout = Timeout(2 * timeout)
  
  import context.dispatcher
  
  val trees = (1 to nTrees).map(i => 
    context.actorOf(NeighborhoodTreeActor.props(points, distance), "tree-" + i)).toList
  
  def receive: Receive = {
    case request @ GetNeighborsRequest(coordinates, k, timeout) => {
      val originalSender = sender
      val aggregator = context.actorOf(NeighborAggregatorActor.props(
          coordinates, k, distance, originalSender, nTrees, timeout))
      trees.foreach(_.tell(request, aggregator))
    }
    case GetNodeStatsRequest(timeout) => {
      val runtime = Runtime.getRuntime

      val mb = 1024 * 1024
      val freeMemoryMB = runtime.freeMemory.toDouble / mb;
      val totalMemoryMB = runtime.totalMemory.toDouble / mb;
      val maxMemoryMB = runtime.maxMemory.toDouble /mb;
      
      val treeStatsFuture = Future.sequence(trees.map(_ ? GetDepthsRequest(timeout)).map {
        _.mapTo[GetDepthsResponse].map { _.depths }.map { depths =>
          TreeStats(minDepth = depths.min, maxDepth = depths.max)
        }
      })
      
      val originalSender = sender
      
      treeStatsFuture.onSuccess { case treeStats: List[TreeStats] =>
        originalSender ! GetNodeStatsResponse(NodeStats(
            freeMemoryMB = freeMemoryMB, 
            totalMemoryMB = totalMemoryMB,
            maxMemoryMB = maxMemoryMB, 
            usedMemoryMB = totalMemoryMB - freeMemoryMB,
            treeStats = treeStats
        ))
      }
    }
  }
}