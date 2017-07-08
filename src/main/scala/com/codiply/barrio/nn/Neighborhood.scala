package com.codiply.barrio.nn

final case class Point(id: String, coordinates: List[Double])

trait Neighborhood {
  def getNeighbors(coordinates: List[Double], k: Int): List[Point]
}