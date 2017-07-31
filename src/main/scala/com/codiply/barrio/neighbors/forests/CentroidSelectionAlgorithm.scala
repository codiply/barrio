package com.codiply.barrio.neighbors.forests

import com.codiply.barrio.geometry.Metric
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.helpers.RandomProvider

trait CentroidSelectionAlgorithm {
  def select(random: RandomProvider, points: List[Point], metric: Metric): Option[(Coordinates, Coordinates)]
}

object CentroidSelectionAlgorithm {
  val randomFurthest: CentroidSelectionAlgorithm = new CentroidSelectionAlgorithm {
    def select(random: RandomProvider, points: List[Point], metric: Metric): Option[(Coordinates, Coordinates)] = {
     for {
       centroid1 <- random.getRandomElement(points).map { _.location }
       centroid2 <- furthestPoint(centroid1, points, metric).map { _.location }
     } yield((centroid1, centroid2))
    }
  }

  private def furthestPoint(from: Coordinates, points: List[Point], metric: Metric): Option[Point] = {
    if (points.isEmpty) {
      None
    } else {
      val (furthest, distance) = points.map { (p: Point) =>
        (p, metric.easyDistance(from, p.location).value) }.maxBy { _._2 }
      if (distance == 0.0) {
        None
      } else {
        Some(furthest)
      }
    }
  }
}
