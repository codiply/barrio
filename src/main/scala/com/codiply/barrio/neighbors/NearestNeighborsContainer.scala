package com.codiply.barrio.neighbors

import scala.annotation.tailrec
import scala.math.max

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.geometry.Point.Coordinates

final case class NearestNeighbor(id: String, location: Option[Coordinates], distance: EasyDistance)

object NearestNeighborsContainer {
  def apply(
      points: List[Point],
      kDesired: Int,
      distanceFunc: Point => EasyDistance,
      includeLocation: Boolean): NearestNeighborsContainer = {
    val distinctPoints = points.groupBy(_.id).map(_._2.head)
    val orderedDistinctNeighbors = distinctPoints.map { p => {
          val location = if (includeLocation) Some(p.location) else None
          NearestNeighbor(p.id, location, distanceFunc(p))
        }
      }.toVector.sortBy(_.distance.value).take(kDesired)
    val distanceUpperBound = getDistanceUpperBound(orderedDistinctNeighbors, kDesired)
    NearestNeighborsContainer(orderedDistinctNeighbors, kDesired, distanceUpperBound)
  }

  def empty(kDesired: Int): NearestNeighborsContainer = NearestNeighborsContainer(
      orderedDistinctNeighbors = Vector.empty,
      kDesired = kDesired,
      distanceUpperBound = None)

  private def mergeOrderedDistinctNeighbors(
      neighbors1: Vector[NearestNeighbor],
      neighbors2: Vector[NearestNeighbor],
      kDesired: Int): Vector[NearestNeighbor] = {
    @tailrec def loop(
        neighbors1: Vector[NearestNeighbor],
        neighbors2: Vector[NearestNeighbor],
        mergedNeighborsReversed: List[NearestNeighbor],
        kRemaining: Int): Vector[NearestNeighbor] = {
      if (kRemaining <= 0) {
        mergedNeighborsReversed.reverse.toVector
      } else {
        (neighbors1, neighbors2) match {
          case (n1 +: ns1, n2 +: ns2) =>
            if (n1.id == n2.id) {
              loop(ns1, ns2, n1 +: mergedNeighborsReversed, kRemaining - 1)
            }
            else {
              if (n1.distance.lessEqualThan(n2.distance)) {
                loop(ns1, neighbors2, n1 +: mergedNeighborsReversed, kRemaining - 1)
              } else {
                loop(neighbors1, ns2, n2 +: mergedNeighborsReversed, kRemaining - 1)
              }
            }
          case (n1 +: ns1, _) => loop(ns1, neighbors2, n1 +: mergedNeighborsReversed, kRemaining - 1)
          case (_, n2 +: ns2) => loop(neighbors1, ns2, n2 +: mergedNeighborsReversed, kRemaining - 1)
          case _ => mergedNeighborsReversed.reverse.toVector
        }
      }
    }

    loop(neighbors1, neighbors2, Nil, kDesired)
  }

  private def getDistanceUpperBound(
      orderedDistinctNeighbors: Vector[NearestNeighbor], kDesired: Int): Option[EasyDistance] = {
    if (orderedDistinctNeighbors.length > 0 && orderedDistinctNeighbors.length == kDesired) {
      Some(orderedDistinctNeighbors.last.distance)
    } else {
      None
    }
  }
}

final case class NearestNeighborsContainer(
    orderedDistinctNeighbors: Vector[NearestNeighbor],
    kDesired: Int,
    distanceUpperBound: Option[EasyDistance]) {
  import NearestNeighborsContainer._

  def merge(that: NearestNeighborsContainer): NearestNeighborsContainer = {
    val newKDesired = max(kDesired, that.kDesired)
    val newOrderedDistinctNeighbors = mergeOrderedDistinctNeighbors(
        orderedDistinctNeighbors, that.orderedDistinctNeighbors, newKDesired)
    val newDistanceUpperBound = getDistanceUpperBound(newOrderedDistinctNeighbors, newKDesired)
    NearestNeighborsContainer(newOrderedDistinctNeighbors, newKDesired, newDistanceUpperBound)
  }

  def count(): Int = orderedDistinctNeighbors.length
}
