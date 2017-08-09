package com.codiply.barrio.tests.geometry

import scala.math.sqrt

import org.scalactic.Tolerance._
import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.PartitioningPlane
import com.codiply.barrio.geometry.Point.Coordinates
import com.codiply.barrio.geometry.RealDistance
import com.codiply.barrio.geometry.Metric

class MetricCosineSpec extends FlatSpec {
  private def pretty(c: Coordinates): String = c.mkString("(", ",", ")")

  val eps = 1e-15

  val metric = Metric.cosine

  val origin = List(0.0, 0.0, 0.0)

  val i = List(2.0, 0.0, 0.0, 0.0)
  val j = List(0.0, 3.0, 0.0, 0.0)
  val k = List(0.0, 0.0, 4.0, 0.0)
  val l = List(0.0, 0.0, 0.0, 5.0)

  val basis = List(i, j, k, l)

  val ij = List(2.0, 2.0, 0.0, 0.0)
  val ik = List(3.0, 0.0, 3.0, 0.0)
  val il = List(4.0, 0.0, 0.0, 4.0)
  val jk = List(0.0, 5.0, 5.0, 0.0)
  val jl = List(0.0, 5.0, 0.0, 5.0)
  val kl = List(0.0, 0.0, 6.0, 6.0)

  val combinations = List(ij, ik, il, jk, jl, kl)

  val a = List(1.0, 2.0, 3.0, 4.0)
  val b = List(3.0, 1.0, 4.0, 2.0)
  val c = List(-2.0, 5.0, -1.0, 3.0)
  val d = List(5.0, -3.0, 1.0, -2.0)

  "easyDistance" should "return the right distance for basis vectors" in {
    for {
      x <- basis
      y1 <- basis
      y2 = Coordinates.scale(-1.0, y1)
    } {
      val distance1 = metric.easyDistance(x, y1)
      val distance2 = metric.easyDistance(x, y2)
      val (expectedDistance1, expectedDistance2) =
        if (x == y1) {
          (EasyDistance(0.0), EasyDistance(2.0))
        } else {
          (EasyDistance(1.0), EasyDistance(1.0))
        }
      assert(distance1 == expectedDistance1, "for coordinates " + pretty(x) + " and " + pretty(y1))
      assert(distance2 == expectedDistance2, "for coordinates " + pretty(x) + " and " + pretty(y2))
    }
  }
  it should "return the right distance (cases 1)" in {
    for {
      x <- combinations
      y <- combinations
    } {
      val distance = metric.easyDistance(x, y)
      val expectedDistance = EasyDistance(if (x == y) { 0.0 } else {
        if (Coordinates.innerProduct(x, y) == 0) 1.0 else 0.75 })
      assert(distance == expectedDistance, "for coordinates " + pretty(x) + " and " + pretty(y))
    }
  }
  it should "return the right distance (case 2)" in {
    val distance = metric.easyDistance(a, b)
    val expectedDistance = 1.0 - (5.0 * 5.0) / (6.0 * 6.0)
    assert(distance.value === expectedDistance +- eps)
  }
  it should "return the right distance (case 3)" in {
    val distance = metric.easyDistance(c, d)
    val expectedDistance = 1.673241288625904
    assert(distance.value === expectedDistance +- eps)
  }

  "easyDistanceToPlane" should "be equal for the two centroids" in {
    for {
      c1 <- List(i, j, a, b)
      c2 <- List(k, l, c, d)
    } {
      val plane = PartitioningPlane(c1, c2)
      val distance = metric.easyDistanceToPlane(plane)
      assert(distance.isDefined, "for centroids " + pretty(c1) + " and " + pretty(c2))
      assert(distance.get(c1).value === distance.get(c2).value +- eps, "for centroids " + pretty(c1) + " and " + pretty(c2))
    }
  }
  it should "be zero for the sum of the two normalized centroids" in {
    for {
      c1 <- List(i, j, a, b)
      c2 <- List(k, l, c, d)
    } {
      val p = for {
        c1hat <- Coordinates.normalize(c1)
        c2hat <- Coordinates.normalize(c2)
      } yield Coordinates.add(c1hat, c2hat)
      val plane = PartitioningPlane(c1, c2)
      val distance = metric.easyDistanceToPlane(plane)
      assert(distance.isDefined, "for centroids " + pretty(c1) + " and " + pretty(c2))
      assert(distance.get(p.get).value === 0.0 +- eps, "for centroids " + pretty(c1) + " and " + pretty(c2))
    }
  }
  it should "return the expected result (cases 1)" in {
    val expectedEasyDistance = 0.25
    val c1 = List(2.0, 0.0, 0.0, 0.0)
    val c2 = List(0.0, 0.0, 3.0, 0.0)
    val plane = PartitioningPlane(c1, c2)
    val distance = metric.easyDistanceToPlane(plane)
    assert(distance.isDefined)
    val points = List(
      List(5.0, 5.0, 0.0, 0.0),
      List(6.0, 0.0, 0.0, 6.0),
      List(0.0, 7.0, 7.0, 0.0),
      List(0.0, 0.0, 8.0, 8.0))
    points.foreach(p => {
      assert(distance.get(p).value === expectedEasyDistance +- eps,
        "for location " + pretty(p))
    })
  }
  it should "return the expected result (cases 2)" in {
    val expectedEasyDistance = 1.0 / 6.0
    val c1 = List(0.0, 0.0, 0.0, 2.0)
    val c2 = List(0.0, 3.0, 0.0, 0.0)
    val plane = PartitioningPlane(c1, c2)
    val distance = metric.easyDistanceToPlane(plane)
    assert(distance.isDefined)
    val points = List(
      List(2.0, 2.0, 2.0, 0.0),
      List(3.0, 0.0, 3.0, 3.0))
    points.foreach(p => {
      assert(distance.get(p).value === expectedEasyDistance +- eps,
        "for location " + pretty(p))
    })
  }
  it should "return the expected result (case 3)" in {
    val expectedEasyDistance = 0.03
    val c1 = List(1.0, 2.0, 2.0, 0.0)
    val c2 = List(2.0, 1.0, 0.0, 2.0)
    val plane = PartitioningPlane(c1, c2)
    val distance = metric.easyDistanceToPlane(plane)
    assert(distance.isDefined)
    val p = List(2.0, 1.0, 3.0, 4.0)
    assert(distance.get(p).value === expectedEasyDistance +- eps)
  }
  it should "return None if the two normalised centroids are equal case" in {
    for {
      c1 <- List(a, b, c, d)
      factor <- List(1.0, 2.0)
      c2 = Coordinates.scale(factor, c1)
    } {
      val plane = PartitioningPlane(c1, c2)
      val distance = metric.easyDistanceToPlane(plane)
      assert(distance == None,
        "for centroids " + pretty(c1) + " and " + pretty(c2))
    }
  }

  "toEasyDistance" should "return None for real distance that is out of bounds" in {
    List(-1.0, -0.001, 2.001, 3.0).foreach(real => {
      val expected = None
      val actual = metric.toEasyDistance(RealDistance(real))
      assert(actual == expected, "for real distance " + real)
    })
  }
  it should "return the same distance for real distance on the bounds and 1.0" in {
    List(0.0, 1.0, 2.0).foreach(real => {
      val expected = Some(EasyDistance(real))
      val actual = metric.toEasyDistance(RealDistance(real))
      assert(actual == expected, "for real distance " + real)
    })
  }
  it should "return the expected value for real distance 0.5" in {
    val real = 0.5
    val easy = 0.75
    val actual = metric.toEasyDistance(RealDistance(real))
    assert(actual.isDefined)
    assert(actual.get.value === easy +- eps)
  }
  it should "return the expected value for real distance 0.7" in {
    val real = 0.7
    val easy = 0.91
    val actual = metric.toEasyDistance(RealDistance(real))
    assert(actual.isDefined)
    assert(actual.get.value === easy +- eps)
  }
  it should "return the expected value for real distance 1.5" in {
    val real = 1.5
    val easy = 1.25
    val actual = metric.toEasyDistance(RealDistance(real))
    assert(actual.isDefined)
    assert(actual.get.value === easy +- eps)
  }
  it should "return the expected value for real distance 1.3" in {
    val real = 1.3
    val easy = 1.09
    val actual = metric.toEasyDistance(RealDistance(real))
    assert(actual.isDefined)
    assert(actual.get.value === easy +- eps)
  }

  "toRealDistance" should "return None for easy distance that is out of bounds" in {
    List(-1.0, -0.001, 2.001, 3.0).foreach(easy => {
      val expected = None
      val actual = metric.toRealDistance(EasyDistance(easy))
      assert(actual == expected, "for easy distance " + easy)
    })
  }
  it should "return the same distance for easy distance on the bounds and 1.0" in {
    List(0.0, 1.0, 2.0).foreach(easy => {
      val expected = Some(RealDistance(easy))
      val actual = metric.toRealDistance(EasyDistance(easy))
      assert(actual == expected, "for easy distance " + easy)
    })
  }
  it should "return the expected value for easy distance 0.36" in {
    val easy = 0.36
    val real = 0.2
    val actual = metric.toRealDistance(EasyDistance(easy))
    assert(actual.isDefined)
    assert(actual.get.value === real +- eps)
  }
  it should "return the expected value for easy distance 0.64" in {
    val easy = 0.64
    val real = 0.4
    val actual = metric.toRealDistance(EasyDistance(easy))
    assert(actual.isDefined)
    assert(actual.get.value === real +- eps)
  }
  it should "return the expected value for easy distance 1.64" in {
    val easy = 1.64
    val real = 1.8
    val actual = metric.toRealDistance(EasyDistance(easy))
    assert(actual.isDefined)
    assert(actual.get.value === real +- eps)
  }
  it should "return the expected value for easy distance 1.36" in {
    val easy = 1.36
    val real = 1.6
    val actual = metric.toRealDistance(EasyDistance(easy))
    assert(actual.isDefined)
    assert(actual.get.value === real +- eps)
  }

  "toRealDistance followed by toEasyDistance" should "return the original easy distance" in {
    (0.1 to 1.9 by 0.1).foreach(easy => {
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
    (0.1 to 1.9 by 0.1).foreach(real => {
      val original = RealDistance(real)
      val actual = for {
        easyDistance <- metric.toEasyDistance(original)
        realDistance <- metric.toRealDistance(easyDistance)
      } yield realDistance
      assert(actual.isDefined, "check is Some for real distance" + real)
      assert(actual.get.value === real +- eps)
    })
  }

  "areValidCoordinates" should "return true for points with non-negative coordinates" in {
    val values = List()
    for {
      p <- List(i, j, k, l, a, b, c, d)
    } {
      assert(metric.areValidCoordinates(p) == true, "for point " + pretty(p))
    }
  }
  "areValidCoordinates" should "return false for the origin" in {
    assert(metric.areValidCoordinates(origin) == false)
  }
  "areValidCoordinates" should "return true in any other case" in {
    val values = List()
    for (p <- List(i, j, k, l, a, b, c, d)) {
      assert(metric.areValidCoordinates(p) == true, "for point " + pretty(p))
    }
  }
}
