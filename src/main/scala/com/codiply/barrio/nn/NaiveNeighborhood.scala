package com.codiply.barrio.nn

class NaiveNeighborhood(
    points: Iterable[Point],
    distance: (List[Double], List[Double]) => Double) extends Neighborhood {
  val allPoints = points.toList
  
  def getNeighbors(coordinates: List[Double], k: Int): List[Point] = 
    allPoints.sortBy(p => distance(p.coordinates, coordinates)).take(k)
}