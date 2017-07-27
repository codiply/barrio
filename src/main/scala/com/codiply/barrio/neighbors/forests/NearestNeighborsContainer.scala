package com.codiply.barrio.neighbors.forests

import scala.annotation.tailrec
import com.codiply.barrio.neighbors.Point

final case class NearestNeighbor(point: Point, distance: Double)

object NearestNeighborsContainer {
  def apply(points: List[Point], kDesired: Int, distanceFunc: Point => Double): NearestNeighborsContainer = {
    val distinctPoints = points.groupBy(_.id).map(_._2.head)
    val orderedDistinctNeighbors = distinctPoints.map { p =>
      NearestNeighbor(p, distanceFunc(p)) }.toVector.sortBy(_.distance).take(kDesired)
    val distanceUpperBound = getDistanceUpperBound(orderedDistinctNeighbors, kDesired)
    NearestNeighborsContainer(orderedDistinctNeighbors, kDesired, distanceUpperBound)
  }
  
  def empty(kDesired: Int) = NearestNeighborsContainer(
      orderedDistinctNeighbors = Vector.empty,
      kDesired = kDesired,
      distanceUpperBound = None)
      
  def mergeOrderedDistinctNeighbors(
      neighbors1: Vector[NearestNeighbor], 
      neighbors2: Vector[NearestNeighbor],
      kDesired: Int): Vector[NearestNeighbor] = {
    @tailrec def loop(
        neighbors1: Vector[NearestNeighbor], 
        neighbors2: Vector[NearestNeighbor],
        newNeighborsReversed: List[NearestNeighbor],
        kRemaining: Int): Vector[NearestNeighbor] = {
      if (kRemaining <= 0) {
        newNeighborsReversed.reverse.toVector
      } else {
        (neighbors1, neighbors2) match {
          case (n1 +: ns1, n2 +: ns2) => 
            if (n1.point.id == n2.point.id)
              loop(ns1, ns2, n1 +: newNeighborsReversed, kRemaining - 1)
            else {
              if (n1.distance < n2.distance)
                loop(ns1, neighbors2, n1 +: newNeighborsReversed, kRemaining - 1)
              else
                loop(neighbors1, ns2, n2 +: newNeighborsReversed, kRemaining - 1)
            }
          case (n1 +: ns1, _) => loop(ns1, neighbors2, n1 +: newNeighborsReversed, kRemaining - 1)
          case (_, n2 +: ns2) => loop(neighbors1, ns2, n2 +: newNeighborsReversed, kRemaining - 1)
          case _ => newNeighborsReversed.reverse.toVector
        }
      }
    }
    
    loop(neighbors1, neighbors2, Nil, kDesired)
  }
  
  def getDistanceUpperBound(
      orderedDistinctNeighbors: Vector[NearestNeighbor], kDesired: Int): Option[Double] = {
    if (orderedDistinctNeighbors.length > 0 && orderedDistinctNeighbors.length == kDesired)
      Some(orderedDistinctNeighbors.last.distance)
    else
      None
  }
}

final case class NearestNeighborsContainer(
    orderedDistinctNeighbors: Vector[NearestNeighbor],
    kDesired: Int,
    distanceUpperBound: Option[Double]) {
  import NearestNeighborsContainer._
  
  def merge(that: NearestNeighborsContainer): NearestNeighborsContainer = {
    val newOrderedDistinctNeighbors = mergeOrderedDistinctNeighbors(
        orderedDistinctNeighbors, that.orderedDistinctNeighbors, kDesired)
    val newDistanceUpperBound = getDistanceUpperBound(newOrderedDistinctNeighbors, kDesired)
    NearestNeighborsContainer(newOrderedDistinctNeighbors, kDesired, newDistanceUpperBound)
  }
}