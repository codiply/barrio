package com.codiply.barrio.tests.neighbors

import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.NearestNeighbor
import com.codiply.barrio.neighbors.NearestNeighborsContainer

class NearestNeighborsContainerSpec extends FlatSpec {
  val point1 = Point("point-1", List(1.0))
  val point2 = Point("point-2", List(2.0))
  val point3 = Point("point-3", List(3.0))
  val point4 = Point("point-4", List(4.0))
  val point5 = Point("point-5", List(5.0))

  val distanceFunc = (p: Point) => EasyDistance(p.location.head * p.location.head)

  val neighbor1 = NearestNeighbor(point1, EasyDistance(1.0))
  val neighbor2 = NearestNeighbor(point2, EasyDistance(4.0))
  val neighbor3 = NearestNeighbor(point3, EasyDistance(9.0))
  val neighbor4 = NearestNeighbor(point4, EasyDistance(16.0))
  val neighbor5 = NearestNeighbor(point5, EasyDistance(25.0))

  val k2Empty = NearestNeighborsContainer.empty(2)
  val k2HalfFull = NearestNeighborsContainer(Vector(neighbor1), 2, None)
  val k2Full = NearestNeighborsContainer(Vector(neighbor1, neighbor2), 2, Some(neighbor2.distance))

  "NearestNeighborsContainer.apply()" should "returns an empty container when called with an empty list" in {
    val kDesired = 2
    val expectedCount = 0

    val actual = NearestNeighborsContainer(Nil, kDesired, distanceFunc)

    assert(actual == k2Empty)

    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 1)" in {
    val kDesired = 4
    val expectedCount = kDesired

    val points = List(point3, point5, point1, point4, point1, point4, point3, point2, point3)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc)
    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3, neighbor4), kDesired, Some(neighbor4.distance))

    assert(actual == expected)
    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 2)" in {
    val kDesired = 4
    val expectedCount = kDesired

    val points = List(point3, point1, point4, point3, point1, point2)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc)
    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3, neighbor4), kDesired, Some(neighbor4.distance))

    assert(actual == expected)
    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 3)" in {
    val kDesired = 4
    val expectedCount = 3

    val points = List(point3, point1, point2, point3, point1, point2)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc)
    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3), kDesired, None)

    assert(actual == expected)
    assert(actual.count() == expectedCount, "test count")
  }

  "NearestNeighborsContainer.merge()" should "return an empty container when merging two empty containers" in {
    assert(k2Empty.merge(k2Empty) == k2Empty)
  }
  it should "return the same container when merged with an empty container" in {
    assert(k2HalfFull.merge(k2Empty) == k2HalfFull, "case 1")
    assert(k2Empty.merge(k2HalfFull) == k2HalfFull, "case 2")
    assert(k2Full.merge(k2Empty) == k2Full, "case 3")
    assert(k2Empty.merge(k2Full) == k2Full, "case 4")
  }
  it should "return the same container when merged with itself" in {
    assert(k2HalfFull.merge(k2HalfFull) == k2HalfFull, "case 1")
    assert(k2Full.merge(k2Full) == k2Full, "case 2")
  }
  it should "remove duplicates and set the distanceUpperBound correctly (case 1)" in {
    val kDesired = 3
    val container1 = NearestNeighborsContainer(List(point2, point3), kDesired, distanceFunc)
    val container2 = NearestNeighborsContainer(List(point1, point2), kDesired, distanceFunc)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3), kDesired, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
  it should "remove duplicates and set the distanceUpperBound correctly (case 2)" in {
    val kDesired = 3

    val container1 = NearestNeighborsContainer(List(point2, point3, point4), kDesired, distanceFunc)
    val container2 = NearestNeighborsContainer(List(point1, point2), kDesired, distanceFunc)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3), 3, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
  it should "remove duplicates and set the distanceUpperBound correctly (case 3)" in {
    val kDesired = 3

    val container1 = NearestNeighborsContainer(List(point2, point3, point4), kDesired, distanceFunc)
    val container2 = NearestNeighborsContainer(List(point1, point2, point5), kDesired, distanceFunc)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3), kDesired, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
}
