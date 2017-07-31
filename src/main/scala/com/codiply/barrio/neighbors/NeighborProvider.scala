package com.codiply.barrio.neighbors

import scala.concurrent.Future

import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.RealDistance

trait NeighborProvider {
  def getNeighbors(location: List[Double], k: Int, distanceThreshold: RealDistance): Future[List[Point]]
  def getStats(): Future[ClusterStats]
}
