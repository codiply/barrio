package com.codiply.barrio.geometry

import scala.math.sqrt

import com.codiply.barrio.geometry.Point.Coordinates

final case class Metric(
    name: String,
    easyDistance: (Coordinates, Coordinates) => EasyDistance,
    easyDistanceToPlane: PartitioningPlane => Option[Coordinates => EasyDistance],
    toEasyDistance: RealDistance => Option[EasyDistance],
    toRealDistance: EasyDistance => Option[RealDistance],
    areValidCoordinates: Coordinates => Boolean)

final object Metric {
  import com.codiply.barrio.geometry.Point.Coordinates._

  val euclidean = Metric(
    name = "euclidean",
    easyDistance = (a: Coordinates, b: Coordinates) => {
      val diff = Coordinates.subtract(a, b)
      val dist = Coordinates.innerProduct(diff, diff)
      EasyDistance(dist)
    },
    easyDistanceToPlane = (plane: PartitioningPlane) => {
      // This is a vector normal to the boundary
      val eta = subtract(plane.centroid1, plane.centroid2)
      val etaLengthSquared = innerProduct(eta, eta)
      if (etaLengthSquared == 0) {
        None
      } else {
        val midPoint = scale(0.5, add(plane.centroid1, plane.centroid2))
        Some((q: Coordinates) => {
          val qToMidpoint = subtract(midPoint, q)
          val product = innerProduct(qToMidpoint, eta)
          EasyDistance((product * product) / etaLengthSquared)
        })
      }
    },
    toEasyDistance = (realDistance: RealDistance) =>
      Some(EasyDistance(realDistance.value * realDistance.value)),
    toRealDistance = (easyDistance: EasyDistance) => {
      if (easyDistance.value >= 0.0) {
        Some(RealDistance(sqrt(easyDistance.value)))
      } else {
        None
      }
    },
    areValidCoordinates = (location: Coordinates) => true)

  val cosine = {
    val easyDistance = (a: Coordinates, b: Coordinates) => {
      val inner = Coordinates.innerProduct(a, b)
      val aSquare = Coordinates.innerProduct(a, a)
      val bSquare = Coordinates.innerProduct(b, b)
      EasyDistance(1 - (inner * inner) / (aSquare * bSquare))
    }
    Metric(
      name = "cosine",
      easyDistance = easyDistance,
      easyDistanceToPlane = (plane: PartitioningPlane) => {
        for {
          c1hat <- Coordinates.normalize(plane.centroid1)
          c2hat <- Coordinates.normalize(plane.centroid2)
          // This is a vector normal to the boundary
          eta = Coordinates.subtract(c1hat, c2hat)
          val etaSquare = Coordinates.innerProduct(eta, eta)
        } yield {
          (location: Coordinates) => {
            val alpha = - Coordinates.innerProduct(location, eta) / etaSquare
            val projection = Coordinates.add(location, Coordinates.scale(alpha, eta))
            easyDistance(location, projection)
          }
        }
      },
      toEasyDistance = (realDistance: RealDistance) => {
        val real = realDistance.value
        if (0.0 <= real && real <= 1.0) {
          Some(EasyDistance(real * (2.0 - real)))
        } else {
          None
        }
      },
      toRealDistance = (easyDistance: EasyDistance) => {
        val easy = easyDistance.value
        if (0.0 <= easy && easy <= 1.0) {
          Some(RealDistance(1 - sqrt(1 - easy)))
        } else {
          None
        }
      },
      areValidCoordinates = (location: Coordinates) => !location.exists(_ < 0.0) && location.exists(_ > 0.0)
    )
  }

  val allMetrics = List(euclidean, cosine).map { m => (m.name, m) }.toMap
}

