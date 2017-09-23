package com.codiply.barrio.neighbors

import scala.concurrent.Future

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.geometry.RealDistance
import com.codiply.barrio.neighbors.Errors.NeighborsRequestError

case class Neighbor(id: String, distance: RealDistance, data: Option[String], location: Option[Coordinates])
case class NeighborsResponse(timeoutReached: Boolean, neighbors: Seq[Neighbor])

trait NeighborProvider {
  def getNeighbors(
      location: Option[Seq[Double]],
      locationId: Option[String],
      k: Int,
      distanceThreshold: Option[RealDistance],
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Option[Long]): Future[Either[NeighborsRequestError, NeighborsResponse]]
  def getHealth(): Future[ClusterHealth]
  def getStats(doGarbageCollect: Boolean): Future[ClusterStats]
}
