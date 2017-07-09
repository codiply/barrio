package com.codiply.barrio.nn

import scala.concurrent.Future

final case class Point(id: String, coordinates: List[Double])

trait Neighborhood {
  def getNeighbors(coordinates: List[Double], k: Int): Future[List[Point]]
}