package com.codiply.barrio.geometry

import scala.math.sqrt

import Point.Coordinates

final case class Point(id: String, location: Coordinates, data: String)
final case class PartitioningPlane(centroid1: Coordinates, centroid2: Coordinates)

final object Point {
  type Coordinates = List[Double]

  final object Coordinates {
    def innerProduct(x: Coordinates, y: Coordinates): Double = x.zip(y).map { t => t._1 * t._2 }.sum
    def subtract(x: Coordinates, y: Coordinates): Coordinates = x.zip(y).map { t => t._1 - t._2 }
    def add(x: Coordinates, y: Coordinates): Coordinates = x.zip(y).map { t => t._1 + t._2 }
    def scale(b: Double, x: Coordinates): Coordinates = x.map(_ * b)
    def normalize(x: Coordinates): Option[Coordinates] = {
      val norm = sqrt(innerProduct(x, x))
      if (norm > 0.0) {
        Some(scale(1 / norm, x))
      } else {
        None
      }
    }
  }
}
