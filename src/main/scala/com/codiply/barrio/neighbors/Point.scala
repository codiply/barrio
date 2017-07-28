package com.codiply.barrio.neighbors

import scala.math.sqrt
import Point._

object Point {
  type Coordinates = List[Double]
  type CoordinatesDistance = (Coordinates, Coordinates) => Double
  type DistanceToBoundary = (Coordinates, Coordinates) => Coordinates => Double

  case class DistanceMetric(
      easyDistance: CoordinatesDistance,
      easyDistanceToBoundary: DistanceToBoundary,
      easyDistanceToRealDistance: Double => Double,
      realDistanceToEasyDistance: Double => Double)

  object DistanceMetric {
    val euclidean = DistanceMetric(
      easyDistance = (coordinates1: Coordinates, coordinates2: Coordinates) =>
        coordinates1.zip(coordinates2).map(t => {
          val diff = t._1 - t._2
          diff * diff
        }).sum,
      easyDistanceToBoundary = (centroid1: Coordinates, centroid2: Coordinates) => {
        // This is a vector normal to the boundary
        val eta = subtractCoordinates(centroid1, centroid2)
        val etaLengthSquared = innerProduct(eta, eta)
        val midPoint = scaleCoordinates(0.5, subtractCoordinates(centroid1, centroid2))
        (coordinates: Coordinates) => {
          val vectorToMidpoint = subtractCoordinates(midPoint, eta)
          val product = innerProduct(vectorToMidpoint, eta)
          (product * product) / etaLengthSquared
        }
      },
      easyDistanceToRealDistance = sqrt,
      realDistanceToEasyDistance = x => x * x)
  }

  def innerProduct(coords1: Coordinates, coords2: Coordinates): Double =
    coords1.zip(coords2).map { t => t._1 * t._2 }.sum
  def subtractCoordinates(coords1: Coordinates, coords2: Coordinates): Coordinates =
    coords1.zip(coords2).map { t => t._1 - t._2 }
  def addCoordinates(coords1: Coordinates, coords2: Coordinates): Coordinates =
    coords1.zip(coords2).map { t => t._1 + t._2 }
  def scaleCoordinates(b: Double, coords: Coordinates): Coordinates =
    coords.map(_ * b)
}

final case class Point(id: String, coordinates: Coordinates)
