package com.codiply.barrio.neighbors.forests

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.LeafStats
import com.codiply.barrio.neighbors.TreeStats

object ActorProtocol {
  final case class InitialiseTree(points: List[Point])
  final case class TreeInitialised(rootTreeName: String)
  final case class NeighborhoodTreeLeafStats(treeName: String, stats: LeafStats)
  final case object GetNeighborhoodTreeStatsRequest
  final case class GetNeighborhoodTreeStatsResponse(treeStats: Map[String, TreeStats])
}
