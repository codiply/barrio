package com.codiply.barrio.nn

case class Point(id: String, coordinates: List[Double])

trait Neighborhood {
  def getNeighbors(k: Int, q: Point): List[Point]
}