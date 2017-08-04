package com.codiply.barrio.tests.geometry

import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.RealDistance

class DistanceSpec extends FlatSpec {
  val one = 1.0
  val two = 2.0
  val three = 3.0

  val easyOne = EasyDistance(one)
  val easyTwo = EasyDistance(two)
  val easyThree = EasyDistance(three)

  "EasyDistance.lessThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { easyOne.lessThan(easyTwo) }
    assertResult(false, "when is actually equal") { easyTwo.lessThan(easyTwo) }
    assertResult(false, "when is actually greater") { easyThree.lessThan(easyTwo) }
  }
  "EasyDistance.lessEqualThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { easyOne.lessEqualThan(easyTwo) }
    assertResult(true, "when is actually equal") { easyTwo.lessEqualThan(easyTwo) }
    assertResult(false, "when is actually greater") { easyThree.lessEqualThan(easyTwo) }
  }
  "EasyDistance.min()" should "do the right thing" in {
    assertResult(easyOne, "when less") { EasyDistance.min(easyOne, easyTwo) }
    assertResult(easyTwo, "when equal") { EasyDistance.min(easyTwo, easyTwo)  }
    assertResult(easyTwo, "when greater") { EasyDistance.min(easyThree, easyTwo)  }
  }

  val realOne = RealDistance(one)
  val realTwo = RealDistance(two)
  val realThree = RealDistance(three)

  "RealDistance.lessThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { realOne.lessThan(realTwo) }
    assertResult(false, "when is actually equal") { realTwo.lessThan(realTwo) }
    assertResult(false, "when is actually greater") { realThree.lessThan(realTwo) }
  }
  "RealDistance.lessEqualThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { realOne.lessEqualThan(realTwo) }
    assertResult(true, "when is actually equal") { realTwo.lessEqualThan(realTwo) }
    assertResult(false, "when is actually greater") { realThree.lessEqualThan(realTwo) }
  }
  "RealDistance.min()" should "do the right thing" in {
    assertResult(realOne, "when less") { RealDistance.min(realOne, realTwo) }
    assertResult(realTwo, "when equal") { RealDistance.min(realTwo, realTwo)  }
    assertResult(realTwo, "when greater") { RealDistance.min(realThree, realTwo)  }
  }
}
