package com.codiply.barrio.geometry

import scala.math.sqrt

import com.codiply.barrio.geometry.Point.Coordinates

final case class Metric(
    easyDistance: (Coordinates, Coordinates) => EasyDistance,
    easyDistanceToPlane: PartitioningPlane => Option[Coordinates => EasyDistance],
    toEasyDistance: RealDistance => Option[EasyDistance],
    toRealDistance: EasyDistance => Option[RealDistance])

final object Metric {
  import com.codiply.barrio.geometry.Point.Coordinates._

  val euclidean = Metric(
    easyDistance = (a: Coordinates, b: Coordinates) => EasyDistance(
      a.zip(b).map(t => {
        val diff = t._1 - t._2
        diff * diff
      }).sum),
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
      if (easyDistance.value < 0.0) {
        None
      } else {
        Some(RealDistance(sqrt(easyDistance.value)))
      }
    })
}

