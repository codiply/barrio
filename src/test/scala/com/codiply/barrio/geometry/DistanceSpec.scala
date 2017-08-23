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

  "EasyDistance.<()" should "do the right thing" in {
    assertResult(true, "when is actually less") { easyOne < easyTwo }
    assertResult(false, "when is actually equal") { easyTwo < easyTwo }
    assertResult(false, "when is actually greater") { easyThree < easyTwo }
  }
  "EasyDistance.>()" should "do the right thing" in {
    assertResult(false, "when is actually less") { easyOne > easyTwo }
    assertResult(false, "when is actually equal") { easyTwo > easyTwo }
    assertResult(true, "when is actually greater") { easyThree > easyTwo }
  }
  "EasyDistance.<=()" should "do the right thing" in {
    assertResult(true, "when is actually less") { easyOne <= easyTwo }
    assertResult(true, "when is actually equal") { easyTwo <= easyTwo }
    assertResult(false, "when is actually greater") { easyThree <= easyTwo }
  }
  "EasyDistance.>=()" should "do the right thing" in {
    assertResult(false, "when is actually less") { easyOne >= easyTwo }
    assertResult(true, "when is actually equal") { easyTwo >= easyTwo }
    assertResult(true, "when is actually greater") { easyThree >= easyTwo }
  }
  "EasyDistance.min()" should "do the right thing" in {
    assertResult(easyOne, "when less") { EasyDistance.min(easyOne, easyTwo) }
    assertResult(easyTwo, "when equal") { EasyDistance.min(easyTwo, easyTwo)  }
    assertResult(easyTwo, "when greater") { EasyDistance.min(easyThree, easyTwo)  }
  }

  val realOne = RealDistance(one)
  val realTwo = RealDistance(two)
  val realThree = RealDistance(three)

  "RealDistance.<()" should "do the right thing" in {
    assertResult(true, "when is actually less") { realOne < realTwo }
    assertResult(false, "when is actually equal") { realTwo < realTwo }
    assertResult(false, "when is actually greater") { realThree < realTwo }
  }
  "RealDistance.>()" should "do the right thing" in {
    assertResult(false, "when is actually less") { realOne > realTwo }
    assertResult(false, "when is actually equal") { realTwo > realTwo }
    assertResult(true, "when is actually greater") { realThree > realTwo }
  }
  "RealDistance.<=()" should "do the right thing" in {
    assertResult(true, "when is actually less") { realOne <= realTwo }
    assertResult(true, "when is actually equal") { realTwo <= realTwo }
    assertResult(false, "when is actually greater") { realThree <= realTwo }
  }
  "RealDistance.>=()" should "do the right thing" in {
    assertResult(false, "when is actually less") { realOne >= realTwo }
    assertResult(true, "when is actually equal") { realTwo >= realTwo }
    assertResult(true, "when is actually greater") { realThree >= realTwo }
  }
  "RealDistance.min()" should "do the right thing" in {
    assertResult(realOne, "when less") { RealDistance.min(realOne, realTwo) }
    assertResult(realTwo, "when equal") { RealDistance.min(realTwo, realTwo)  }
    assertResult(realTwo, "when greater") { RealDistance.min(realThree, realTwo)  }
  }
}
