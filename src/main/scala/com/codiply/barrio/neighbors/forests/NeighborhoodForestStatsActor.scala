package com.codiply.barrio.neighbors.forests

import akka.actor.Actor
import akka.actor.Props
import com.codiply.barrio.helpers.LongQuantityStats
import com.codiply.barrio.neighbors.TreeStats
import com.codiply.barrio.neighbors.TreeStatsContainer

object NeihborhoodForestStatsActor {
  def props(): Props = Props(new NeighborhoodForestStatsActor())
}

class NeighborhoodForestStatsActor extends Actor {
  import ActorProtocol._

  var treeStatsContainers = Map[String, TreeStatsContainer]()

  def receive: Receive = {
    case leafStats: NeighborhoodTreeLeafStats =>
      val previousStats = treeStatsContainers.getOrElse(leafStats.treeName, TreeStatsContainer.empty)
      val newStats = previousStats.add(leafStats.stats)
      treeStatsContainers = treeStatsContainers + (leafStats.treeName -> newStats)
    case GetNeighborhoodTreeStatsRequest =>
      sender ! GetNeighborhoodTreeStatsResponse(treeStatsContainers.mapValues(_.toStats))
  }
}
