package com.codiply.barrio.neighbors

import scala.concurrent.Future

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.geometry.RealDistance

case class Neighbor(id: String, location: Option[Coordinates], distance: RealDistance)

trait NeighborProvider {
  def getNeighbors(
      location: List[Double],
      k: Int,
      distanceThreshold: RealDistance,
      includeLocation: Boolean,
      timeoutMilliseconds: Option[Int]): Future[Vector[Neighbor]]
  def getHealth(): Future[ClusterHealth]
  def getStats(doGarbageCollect: Boolean): Future[ClusterStats]
}
