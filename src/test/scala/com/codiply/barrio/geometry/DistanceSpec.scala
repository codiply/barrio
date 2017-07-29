package com.codiply.barrio.tests.geometry

import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.EasyDistance
import com.codiply.barrio.geometry.RealDistance

class DistanceSpec extends FlatSpec {
  "EasyDistance.lessThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { EasyDistance(1.0).lessThan(EasyDistance(2.0)) }
    assertResult(false, "when is actually equal") { EasyDistance(2.0).lessThan(EasyDistance(2.0)) }
    assertResult(false, "when is actually greater") { EasyDistance(3.0).lessThan(EasyDistance(2.0)) }
  }
  "EasyDistance.lessEqualThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { EasyDistance(1.0).lessEqualThan(EasyDistance(2.0)) }
    assertResult(true, "when is actually equal") { EasyDistance(2.0).lessEqualThan(EasyDistance(2.0)) }
    assertResult(false, "when is actually greater") { EasyDistance(3.0).lessEqualThan(EasyDistance(2.0)) }
  }

  "RealDistance.lessThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { RealDistance(1.0).lessThan(RealDistance(2.0)) }
    assertResult(false, "when is actually equal") { RealDistance(2.0).lessThan(RealDistance(2.0)) }
    assertResult(false, "when is actually greater") { RealDistance(3.0).lessThan(RealDistance(2.0)) }
  }
  "RealDistance.lessEqualThan()" should "do the right thing" in {
    assertResult(true, "when is actually less") { RealDistance(1.0).lessEqualThan(RealDistance(2.0)) }
    assertResult(true, "when is actually equal") { RealDistance(2.0).lessEqualThan(RealDistance(2.0)) }
    assertResult(false, "when is actually greater") { RealDistance(3.0).lessEqualThan(RealDistance(2.0)) }
  }
}
