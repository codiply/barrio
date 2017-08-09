package com.codiply.barrio.tests.geometry

import org.scalatest.FlatSpec

import com.codiply.barrio.geometry.Metric

class MetricSpec extends FlatSpec {
  "allMetrics" should "contain keys that are all lowercase" in {
    val keys = Metric.allMetrics.keys
    keys.foreach(key => assert(key.toLowerCase == key))
  }
  it should "contain metrics with name all in lowercase" in {
    val names = Metric.allMetrics.values.map(_.name)
    names.foreach(name => assert(name.toLowerCase == name))
  }
}
