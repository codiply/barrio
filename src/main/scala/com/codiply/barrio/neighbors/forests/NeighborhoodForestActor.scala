package com.codiply.barrio.neighbors.forests

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
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
    nTrees: Int) extends Actor with ActorLogging {
  import ActorProtocol._
  import com.codiply.barrio.neighbors.MemoryStats
  import com.codiply.barrio.neighbors.TreeStats
  import com.codiply.barrio.neighbors.NodeStats
  
  val timeout: FiniteDuration = 5 seconds
  implicit val askTimeout = Timeout(2 * timeout)
  
  import context.dispatcher
  
  val statsActor = context.actorOf(NeihborhoodForestStatsActor.props(), "stats-actor")
  
  val trees = (1 to nTrees).map(i => {
    val name = "tree-" + i
    context.actorOf(NeighborhoodTreeActor.props(name, points, distance, 0, statsActor), name)
  }).toList
  
  var initialisedTreesCount = 0
  var initialisedTrees: List[ActorRef] = Nil
    
  def receive: Receive = {
    case TreeInitialised => {
      if (trees.contains(sender)) {
        initialisedTreesCount += 1
        initialisedTrees = sender +: initialisedTrees
      }
    }
    case request @ GetNeighborsRequest(coordinates, k, timeout) => {
      val originalSender = sender
      val aggregator = context.actorOf(NeighborAggregatorActor.props(
          coordinates, k, distance, originalSender, initialisedTreesCount, timeout))
      initialisedTrees.foreach(_.tell(request, aggregator))
    }
    case GetNodeStatsRequest(timeout) => {
      val runtime = Runtime.getRuntime

      val mb = 1024 * 1024
      val freeMemoryMB = runtime.freeMemory.toDouble / mb;
      val totalMemoryMB = runtime.totalMemory.toDouble / mb;
      val maxMemoryMB = runtime.maxMemory.toDouble /mb;
      
      val treeStatsResponse = (statsActor ? GetNeighborhoodTreeStatsRequest).mapTo[GetNeighborhoodTreeStatsResponse]
      
      val originalSender = sender
      
      treeStatsResponse.map{ _.treeStats } onSuccess { case treeStats: Map[String, TreeStats] =>
        originalSender ! GetNodeStatsResponse(NodeStats(
            memory = MemoryStats(
                freeMemoryMB = freeMemoryMB, 
                totalMemoryMB = totalMemoryMB,
                 maxMemoryMB = maxMemoryMB, 
              usedMemoryMB = totalMemoryMB - freeMemoryMB),
            trees = treeStats
        ))
      }
    }
  }
}