package com.codiply.barrio.neighbors

import scala.concurrent.Future

trait NeighborProvider {
  def getNeighbors(coordinates: List[Double], k: Int): Future[List[Point]]
  def getStats(): Future[ClusterStats]
}