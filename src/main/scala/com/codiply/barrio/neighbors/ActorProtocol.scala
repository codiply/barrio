package com.codiply.barrio.neighbors

import scala.concurrent.duration.FiniteDuration
import Point._

object ActorProtocol {
  final case class GetNeighborsRequest(
      coordinates: List[Double], k: Int, distanceThreshold: Double, timeout: FiniteDuration)
  final case class GetNeighborsResponse(neighbors: List[Point])
  final case class GetClusterStatsRequest(timeout: FiniteDuration)
  final case class GetNodeStatsRequest(timeout: FiniteDuration)
  final case class GetClusterStatsResponse(stats: ClusterStats)
  final case class GetNodeStatsResponse(stats: NodeStats)
}