package com.codiply.barrio.neighbors

import Point._

object Point {
  type Coordinates = List[Double]
  type DistanceMetric = (Coordinates, Coordinates) => Double
  
  object DistanceMetric {
    val Euclidean = (coordinates1: Coordinates, coordinates2: Coordinates) =>
      coordinates1.zip(coordinates2).map(t => {
        val diff = t._1 - t._2
        diff * diff
      }).sum
  }
}

final case class Point(id: String, coordinates: Coordinates)