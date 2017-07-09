package com.codiply.barrio.nn

import scala.concurrent.{ Future, ExecutionContext}
import ExecutionContext.Implicits.global

class NaiveNeighborhood(
    points: Iterable[Point],
    distance: (List[Double], List[Double]) => Double) extends Neighborhood {
  val allPoints = points.toList
  
  def getNeighbors(coordinates: List[Double], k: Int): Future[List[Point]] = Future {
    allPoints.sortBy(p => distance(p.coordinates, coordinates)).take(k)
  }
}