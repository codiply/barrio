package com.codiply.barrio.tests.geometry

import scala.math.sqrt

import org.scalactic.Tolerance._
import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.PartitioningPlane
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.geometry.RealDistance
import com.codiply.barrio.geometry.Metric

class MetricEuclideanSpec extends FlatSpec {
  private def pretty(c: Coordinates): String = c.mkString("(", ",", ")")

  val eps = 1e-15

  val metric = Metric.euclidean

  val origin = List(0.0, 0.0, 0.0)

  val i = List(1.0, 0.0, 0.0)
  val j = List(0.0, 1.0, 0.0)
  val k = List(0.0, 0.0, 1.0)
  val basis = List(i, j, k)

  "easyDistance for base vector" should "return 1.0 for distance from origin" in {
    for (x <- basis) {
      assert(metric.easyDistance(origin, x) == EasyDistance(1.0), "for coordinates " + pretty(x))
    }
  }
  it should "return the right distance from another base vector" in {
    for {
      x <- basis
      y <- basis
    } {
      val distance = metric.easyDistance(x, y)
      val expectedDistance = EasyDistance(if (x == y) 0.0 else 2.0)
      assert(distance == expectedDistance, "for coordinates " + pretty(x) + " and " + pretty(y))
    }
  }

  "easyDistanceToPlane" should "be equal for the two centroids" in {
    for {
      c1 <- List(List(1.0, 2.0, 3.0), List(-3.0, -2.0, -1.0))
      c2 <- List(List(-1.0, -2.0, -3.0), List(3.0, -2.0, -1.0))
    } {
      val plane = PartitioningPlane(c1, c2)
      val distance = metric.easyDistanceToPlane(plane)
      assert(distance.isDefined, "for centroids " + pretty(c1) + " and " + pretty(c2))
      assert(distance.get(c1) == distance.get(c2), "for centroids " + pretty(c1) + " and " + pretty(c2))
    }
  }
  it should "be zero for the mid-point between the centroids" in {
    for {
      c1 <- List(List(1.0, 2.0, 3.0), List(-3.0, -2.0, -1.0))
      c2 <- List(List(-1.0, -2.0, -3.0), List(3.0, -2.0, -1.0))
    } {
      val plane = PartitioningPlane(c1, c2)
      val distance = metric.easyDistanceToPlane(plane)
      val midPoint = Coordinates.scale(0.5, Coordinates.add(c1, c2))
      assert(distance.isDefined, "for centroids " + pretty(c1) + " and " + pretty(c2))
      assert(distance.get(midPoint) == EasyDistance(0.0), "for centroids " + pretty(c1) + " and " + pretty(c2))
    }
  }
  it should "give the right result" in {
    val c1 = List(-1.0, 1.0)
    val c2 = List(1.0, -1.0)
    val plane = PartitioningPlane(c1, c2)
    val distance = metric.easyDistanceToPlane(plane)
    assert(distance.isDefined)
    assert(distance.get(List(2.0, 2.0)) == EasyDistance(0.0), "case 1")
    assert(distance.get(List(1.0, 0.0)) == EasyDistance(0.5), "case 2")
    assert(distance.get(List(0.0, 1.0)) == EasyDistance(0.5), "case 3")
    assert(distance.get(List(1.0, 2.0)) == EasyDistance(0.5), "case 4")
    assert(distance.get(List(2.0, 1.0)) == EasyDistance(0.5), "case 5")
  }
  it should "not be defined for two centroids with the same coordinates" in {
    for (c <- List(List(1.0, 2.0, 3.0), List(-3.0, -2.0, -1.0))) {
      val plane = PartitioningPlane(c, c)
      val distance = metric.easyDistanceToPlane(plane)
      assert(!distance.isDefined, "for centroid " + pretty(c))
    }
  }

  "toEasyDistance" should "return the expected result" in {
    List(0.0, 1.0, 2.0).foreach(real => {
      val expected = Some(EasyDistance(real * real))
      val actual = metric.toEasyDistance(RealDistance(real))
      assert(actual == expected, "for real distance " + real)
    })
  }

  "toRealDistance" should "return the expected result for non-negative easy distance" in {
    List(0.0, 1.0, 2.0).foreach(easy => {
      val expected = Some(RealDistance(sqrt(easy)))
      val actual = metric.toRealDistance(EasyDistance(easy))
      assert(actual == expected, "for easy distance " + easy)
    })
  }
  it should "return None when real distance is negative" in {
    List(-1.0, -2.0).foreach(easy => {
      assert(metric.toRealDistance(EasyDistance(easy)) == None, "for easy distance " + easy)
    })
  }

  "toRealDistance followed by toEasyDistance" should "return the original easy distance" in {
    (0.0 to 10.0 by 1.0).foreach(easy => {
      val original = EasyDistance(easy)
      val actual = for {
        realDistance <- metric.toRealDistance(original)
        easyDistance <- metric.toEasyDistance(realDistance)
      } yield easyDistance
      assert(actual.isDefined, "check is Some for easy distance" + easy)
      assert(actual.get.value === easy +- eps)
    })
  }

  "toEasyDistance followed by toRealDistance" should "return the original real distance" in {
    (0.0 to 10.0 by 1.0).foreach(real => {
      val original = RealDistance(real)
      val actual = for {
        easyDistance <- metric.toEasyDistance(original)
        realDistance <- metric.toRealDistance(easyDistance)
      } yield realDistance
      assert(actual.isDefined, "check is Some for real distance" + real)
      assert(actual.get.value === real +- eps)
    })
  }

  "areValidCoordinates" should "return true in all cases" in {
    val values = List(-1.0, 0.0, 1.0)
    for {
      x <- values
      y <- values
      z <- values
      p = List(x, y, z)
    } {
      assert(metric.areValidCoordinates(p), "for point " + pretty(p))
    }
  }
}
