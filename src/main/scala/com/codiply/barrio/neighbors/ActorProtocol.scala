package com.codiply.barrio.neighbors

import Point._

object ActorProtocol {
  final case class GetNeighborsRequest(coordinates: List[Double], k: Int)
  final case class GetNeighborsResponse(neighbors: List[Point])
  final case object GetClusterStatsRequest
  final case object GetNodeStatsRequest
  final case class GetClusterStatsResponse(stats: ClusterStats)
  final case class GetNodeStatsResponse(stats: NodeStats)
}