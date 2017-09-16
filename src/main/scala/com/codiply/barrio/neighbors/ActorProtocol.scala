package com.codiply.barrio.neighbors

import scala.concurrent.duration.FiniteDuration

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates

object ActorProtocol {
  sealed trait GetNeighborsRequest
  final case class GetNeighborsRequestByLocationId(
      locationId: String,
      k: Int,
      distanceThreshold: EasyDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Int) extends GetNeighborsRequest
  final case class GetNeighborsRequestByLocation(
      location: Coordinates,
      k: Int,
      distanceThreshold: EasyDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Int) extends GetNeighborsRequest
  final case class GetNeighborsResponse(timeoutReached: Boolean, distanceThreshold: EasyDistance, neighbors: Vector[NearestNeighbor])
  final case class GetClusterStatsRequest(timeoutMilliseconds: Int, doGarbageCollect: Boolean)
  final case class GetNodeStatsRequest(timeoutMilliseconds: Int, doGarbageCollect: Boolean)
  final case class GetClusterStatsResponse(stats: ClusterStats)
  final case class GetNodeStatsResponse(nodeName: String, stats: NodeStats)
}
