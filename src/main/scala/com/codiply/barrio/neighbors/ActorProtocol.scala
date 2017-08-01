package com.codiply.barrio.neighbors

import scala.concurrent.duration.FiniteDuration

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Point

object ActorProtocol {
  final case class GetNeighborsRequest(
      location: List[Double], k: Int, distanceThreshold: EasyDistance, timeout: FiniteDuration)
  final case class GetNeighborsResponse(neighbors: List[Point])
  final case class GetClusterStatsRequest(timeout: FiniteDuration, doGarbageCollect: Boolean)
  final case class GetNodeStatsRequest(timeout: FiniteDuration, doGarbageCollect: Boolean)
  final case class GetClusterStatsResponse(stats: ClusterStats)
  final case class GetNodeStatsResponse(nodeName: String, stats: NodeStats)
}
