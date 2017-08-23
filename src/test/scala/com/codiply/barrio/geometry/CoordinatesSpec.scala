package com.codiply.barrio.tests.geometry

import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.Point.Coordinates

class CoordinatesSpec extends FlatSpec {
  private def pretty(c: Coordinates): String = c.mkString("(", ",", ")")

  val zero = Coordinates(0.0, 0.0, 0.0)

  val i = Coordinates(1.0, 0.0, 0.0)
  val j = Coordinates(0.0, 1.0, 0.0)
  val k = Coordinates(0.0, 0.0, 1.0)
  val basis = List(i, j, k)

  "Coordinates.add()" should "return the coordinates when adding zero" in {
    for (x <- basis) {
      assert(Coordinates.add(x, zero) == x, "for coordinates " + pretty(x))
    }
  }
  it should "be equivalent to scaling with 2 when adding coordinates to oneself" in {
    for (x <- basis) {
      assert(Coordinates.add(x, x) == Coordinates.scale(2.0, x), "for coordinates " + pretty(x))
    }
  }

  "Coordinates.subtract()" should "result to zero when subtracting coordinates from itself" in {
    for (x <- basis) {
      assert(Coordinates.subtract(x, x) == zero, "for coordinates " + pretty(x))
    }
  }
  it should "return the coordinates when subtracting zero" in {
    for (x <- basis) {
      assert(Coordinates.subtract(x, zero) == x, "for coordinates " + pretty(x))
    }
  }

  "Coordinates.scale()" should "result to zero when scaled by zero" in {
    for (x <- basis) {
      assert(Coordinates.scale(0.0, x) == zero, "for coordinates " + pretty(x))
    }
  }
  it should "result to opposite coordinates when scaled with -1" in {
    for (x <- basis) {
      val opposite = Coordinates.scale(-1.0, x)
      assert(Coordinates.add(x, opposite) == zero, "for coordinates " + pretty(x))
    }
  }

  "Coordinates.innerProduct()" should "give the expected result for basis vectors" in {
    for {
      x <- basis
      y <- basis
      } {
        val innerProduct = Coordinates.innerProduct(x, y)
        val expectedInnerProduct = if (x == y) 1.0 else 0.0
        assert(innerProduct == expectedInnerProduct, "for coordinates " + pretty(x) + " and " + pretty(y))
    }
  }
  it should "give the expected result for a unit vector with the opposite of another unit vector" in {
    for {
      x <- basis
      y <- basis
      } {
        val innerProduct = Coordinates.innerProduct(x, Coordinates.scale(-1, y))
        val expectedInnerProduct = if (x == y) -1.0 else 0.0
        assert(innerProduct == expectedInnerProduct, "for coordinates " + pretty(x) + " and " + pretty(y))
    }
  }
}
