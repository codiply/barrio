package com.codiply.barrio.neighbors.forests

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.TreeStats

object ActorProtocol {
  final case class InitialiseTree(points: List[Point])
  final case object TreeInitialised
  final case class NeighborhoodTreeLeafStats(treeName: String, depth: Int, pointCount: Int)
  final case object GetNeighborhoodTreeStatsRequest
  final case class GetNeighborhoodTreeStatsResponse(treeStats: Map[String, TreeStats])
}
