package com.codiply.barrio.nn

class NaiveNeighborhood(
    points: List[Point],
    distance: Point => Point => Double) extends Neighborhood {
  def getNeighbors(k: Int, q: Point): List[Point] = points.sortBy(distance(q)).take(k)
}