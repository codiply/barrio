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

import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.RealDistance
import com.codiply.barrio.helpers.RandomProvider
import com.codiply.barrio.neighbors.ActorProtocol._
import com.codiply.barrio.neighbors.NeighborhoodConfig
import com.codiply.barrio.neighbors.NeighborAggregatorActor
import com.codiply.barrio.neighbors.NodeStats
import com.codiply.barrio.neighbors.TreeStats

object NeighborhoodForestActor {
  def props(
    name: String,
    points: List[Point],
    config: NeighborhoodConfig,
    random: RandomProvider): Props =
      Props(new NeighborhoodForestActor(name, points, config, random))
}

class NeighborhoodForestActor(
    name: String,
    points: List[Point],
    config: NeighborhoodConfig,
    random: RandomProvider) extends Actor with ActorLogging {
  import ActorProtocol._
  import com.codiply.barrio.neighbors.MemoryStats
  import com.codiply.barrio.neighbors.TreeStats
  import com.codiply.barrio.neighbors.NodeStats

  val timeout: FiniteDuration = 5.seconds
  implicit val askTimeout = Timeout(2 * timeout)

  import context.dispatcher

  val statsActor = context.actorOf(NeihborhoodForestStatsActor.props(), "stats-actor")

  val trees = (1 to config.treesPerNode).map(i => {
    val name = "tree-" + i
    context.actorOf(NeighborhoodTreeActor.props(name, config, random.createNew(), 0, statsActor), name)
  }).toList

  trees.foreach { _ ! InitialiseTree(points) }

  var initialisedTreesCount = 0
  var initialisedTrees: List[ActorRef] = Nil

  def receive: Receive = {
    case TreeInitialised => {
      if (trees.contains(sender)) {
        initialisedTreesCount += 1
        initialisedTrees = sender +: initialisedTrees
      }
    }
    case request @ GetNeighborsRequest(location, k, distanceThreshold, timeout) => {
      val originalSender = sender
      val searchActor = context.actorOf(NeighborhoodForestSearchActor.props(
          originalSender, initialisedTrees, location, k, distanceThreshold, timeout))
    }
    case GetNodeStatsRequest(timeout, doGarbageCollect) => {
      val runtime = Runtime.getRuntime

      if (doGarbageCollect) {
        runtime.gc()
      }

      val mb = 1024 * 1024
      val freeMemoryMB = runtime.freeMemory.toDouble / mb;
      val totalMemoryMB = runtime.totalMemory.toDouble / mb;
      val maxMemoryMB = runtime.maxMemory.toDouble /mb;

      val treeStatsResponse = (statsActor ? GetNeighborhoodTreeStatsRequest).mapTo[GetNeighborhoodTreeStatsResponse]

      val originalSender = sender

      treeStatsResponse.map{ _.treeStats } onSuccess { case treeStats: Map[String, TreeStats] =>
        originalSender ! GetNodeStatsResponse(name, NodeStats(
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
