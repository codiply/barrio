package com.codiply.barrio.neighbors

import scala.concurrent.Future

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.geometry.RealDistance

case class Neighbor(id: String, distance: RealDistance, data: Option[String], location: Option[Coordinates])

trait NeighborProvider {
  def getNeighbors(
      location: Option[List[Double]],
      locationId: Option[String],
      k: Int,
      distanceThreshold: RealDistance,
      includeData: Boolean,
      includeLocation: Boolean,
      timeoutMilliseconds: Option[Int]): Future[Vector[Neighbor]]
  def getHealth(): Future[ClusterHealth]
  def getStats(doGarbageCollect: Boolean): Future[ClusterStats]
}
