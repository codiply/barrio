package com.codiply.barrio.neighbors.forests

import akka.actor.Actor
import akka.actor.Props
import com.codiply.barrio.neighbors.TreeStats

object NeihborhoodForestStatsActor {
  def props(): Props = Props(new NeighborhoodForestStatsActor())
}

class NeighborhoodForestStatsActor extends Actor {
  import ActorProtocol._

  var treeStats = Map[String, TreeStats]()

  val initialStats = TreeStats(
      leafs = 0, points = 0,
      minLeafPoints = Int.MaxValue, meanLeafPoints = 0.0, maxLeafPoints = 0,
      minDepth = Int.MaxValue, meanDepth = 0.0, maxDepth = 0)

  def receive: Receive = {
    case leafStats: NeighborhoodTreeLeafStats =>
      val previousStats = treeStats.getOrElse(leafStats.treeName, initialStats)
      val newLeafs = previousStats.leafs + 1
      val newPoints = previousStats.points + leafStats.pointCount
      val newStats = TreeStats(
          leafs = newLeafs,
          points = newPoints,
          minLeafPoints = Math.min(previousStats.minLeafPoints, leafStats.pointCount),
          meanLeafPoints = newPoints.toDouble / newLeafs,
          maxLeafPoints = Math.max(previousStats.maxLeafPoints, leafStats.pointCount),
          minDepth = Math.min(previousStats.minDepth, leafStats.depth),
          meanDepth = (previousStats.meanDepth * previousStats.leafs + leafStats.depth) / newLeafs,
          maxDepth = Math.max(previousStats.maxDepth, leafStats.depth))
      treeStats = treeStats + (leafStats.treeName -> newStats)
    case GetNeighborhoodTreeStatsRequest =>
      sender ! GetNeighborhoodTreeStatsResponse(treeStats)
  }
}
