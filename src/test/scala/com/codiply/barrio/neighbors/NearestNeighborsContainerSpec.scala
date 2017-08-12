package com.codiply.barrio.tests.neighbors

import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.Point
import com.codiply.barrio.neighbors.NearestNeighbor
import com.codiply.barrio.neighbors.NearestNeighborsContainer

class NearestNeighborsContainerSpec extends FlatSpec {
  val point1 = Point("point-1", List(1.0), "data-1")
  val point2 = Point("point-2", List(2.0), "data-2")
  val point3 = Point("point-3", List(3.0), "data-3")
  val point4 = Point("point-4", List(4.0), "data-4")
  val point5 = Point("point-5", List(5.0), "data-5")

  val distanceFunc = (p: Point) => EasyDistance(p.location.head * p.location.head)

  val neighbor1 = NearestNeighbor(point1.id, EasyDistance(1.0), Some(point1.data), Some(point1.location))
  val neighbor2 = NearestNeighbor(point2.id, EasyDistance(4.0), Some(point2.data), Some(point2.location))
  val neighbor3 = NearestNeighbor(point3.id, EasyDistance(9.0), Some(point3.data), Some(point3.location))
  val neighbor4 = NearestNeighbor(point4.id, EasyDistance(16.0), Some(point4.data), Some(point4.location))
  val neighbor5 = NearestNeighbor(point5.id, EasyDistance(25.0), Some(point5.data), Some(point5.location))

  val neighbor1NoLocation = neighbor1.copy(location = None)
  val neighbor2NoLocation = neighbor2.copy(location = None)
  val neighbor3NoLocation = neighbor3.copy(location = None)
  val neighbor4NoLocation = neighbor4.copy(location = None)
  val neighbor5NoLocation = neighbor5.copy(location = None)

  val neighbor1NoData = neighbor1.copy(data = None)
  val neighbor2NoData = neighbor2.copy(data = None)
  val neighbor3NoData = neighbor3.copy(data = None)
  val neighbor4NoData = neighbor4.copy(data = None)
  val neighbor5NoData = neighbor5.copy(data = None)

  val k2Empty = NearestNeighborsContainer.empty(2)
  val k2HalfFull = NearestNeighborsContainer(Vector(neighbor1), 2, None)
  val k2Full = NearestNeighborsContainer(Vector(neighbor1, neighbor2), 2, Some(neighbor2.distance))

  "NearestNeighborsContainer.apply()" should "returns an empty container when called with an empty list" in {
    val kDesired = 2
    val expectedCount = 0
    val includeData = true
    val includeLocation = true

    val actual = NearestNeighborsContainer(Nil, kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)

    assert(actual == k2Empty)

    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 1 - with location and data)" in {
    val kDesired = 4
    val expectedCount = kDesired
    val includeData = true
    val includeLocation = true

    val points = List(point3, point5, point1, point4, point1, point4, point3, point2, point3)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3, neighbor4), kDesired, Some(neighbor4.distance))

    assert(actual == expected)
    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 1 - without location)" in {
    val kDesired = 4
    val expectedCount = kDesired
    val includeData = true
    val includeLocation = false

    val points = List(point3, point5, point1, point4, point1, point4, point3, point2, point3)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val expected = NearestNeighborsContainer(
        Vector(neighbor1NoLocation, neighbor2NoLocation, neighbor3NoLocation, neighbor4NoLocation),
        kDesired, Some(neighbor4.distance))

    assert(actual == expected)
    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 1 - without data)" in {
    val kDesired = 4
    val expectedCount = kDesired
    val includeData = false
    val includeLocation = true

    val points = List(point3, point5, point1, point4, point1, point4, point3, point2, point3)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val expected = NearestNeighborsContainer(
        Vector(neighbor1NoData, neighbor2NoData, neighbor3NoData, neighbor4NoData),
        kDesired, Some(neighbor4.distance))

    assert(actual == expected)
    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 2)" in {
    val kDesired = 4
    val expectedCount = kDesired
    val includeData = true
    val includeLocation = true

    val points = List(point3, point1, point4, point3, point1, point2)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3, neighbor4), kDesired, Some(neighbor4.distance))

    assert(actual == expected)
    assert(actual.count() == expectedCount, "test count")
  }
  it should "keep the nearest distinct neighbors (case 3)" in {
    val kDesired = 4
    val expectedCount = 3
    val includeData = true
    val includeLocation = true

    val points = List(point3, point1, point2, point3, point1, point2)
    val actual = NearestNeighborsContainer(points, kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
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
  it should "remove duplicates and set the distanceUpperBound correctly (case 1 - with location and data)" in {
    val kDesired = 3
    val includeData = true
    val includeLocation = true

    val container1 = NearestNeighborsContainer(List(point2, point3), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val container2 = NearestNeighborsContainer(List(point1, point2), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3), kDesired, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
  it should "remove duplicates and set the distanceUpperBound correctly (case 1 - without location)" in {
    val kDesired = 3
    val includeData = true
    val includeLocation = false

    val container1 = NearestNeighborsContainer(List(point2, point3), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val container2 = NearestNeighborsContainer(List(point1, point2), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1NoLocation, neighbor2NoLocation, neighbor3NoLocation), kDesired, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
  it should "remove duplicates and set the distanceUpperBound correctly (case 1 - without data)" in {
    val kDesired = 3
    val includeData = false
    val includeLocation = true

    val container1 = NearestNeighborsContainer(List(point2, point3), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val container2 = NearestNeighborsContainer(List(point1, point2), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1NoData, neighbor2NoData, neighbor3NoData), kDesired, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
  it should "remove duplicates and set the distanceUpperBound correctly (case 2)" in {
    val kDesired = 3
    val includeData = true
    val includeLocation = true

    val container1 = NearestNeighborsContainer(List(point2, point3, point4), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val container2 = NearestNeighborsContainer(List(point1, point2), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3), 3, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
  it should "remove duplicates and set the distanceUpperBound correctly (case 3)" in {
    val kDesired = 3
    val includeData = true
    val includeLocation = true

    val container1 = NearestNeighborsContainer(List(point2, point3, point4), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)
    val container2 = NearestNeighborsContainer(List(point1, point2, point5), kDesired, distanceFunc,
        includeData = includeData, includeLocation = includeLocation)

    val merged1 = container1.merge(container2)
    val merged2 = container2.merge(container1)

    val expected = NearestNeighborsContainer(
        Vector(neighbor1, neighbor2, neighbor3), kDesired, Some(neighbor3.distance))

    assert(merged1 == expected, "merged1")
    assert(merged2 == expected, "merged2")
  }
}
