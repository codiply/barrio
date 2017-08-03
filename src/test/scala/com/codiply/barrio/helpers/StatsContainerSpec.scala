package com.codiply.barrio.tests.helpers

import org.scalatest.FlatSpec

import com.codiply.barrio.helpers.LongQuantityStats
import com.codiply.barrio.helpers.LongQuantityStatsContainer

class LongQuantityStatsContainerSpec extends FlatSpec {
  "LongQuantityStatsContainer.empty" should "return an empty container with expected stats" in {
    val container = LongQuantityStatsContainer.empty

    assert(container.count == 0, "when testing count")

    val stats = container.toStats

    assert(stats.min == Long.MaxValue, "when testing min")
    assert(stats.max == Long.MinValue, "when testing max")
    assert(stats.mean == 0.0, "when testing mean")
    assert(stats.standardError == Double.MaxValue, "when testing standardError")
  }
  it should "return a container with expected stats after adding one value" in {
    val value = 3

    val container = LongQuantityStatsContainer.empty.add(value)

    assert(container.count == 1, "when testing count")

    val stats = container.toStats

    assert(stats.min == value, "when testing min")
    assert(stats.max == value, "when testing max")
    assert(stats.mean == value.toDouble, "when testing mean")
    assert(stats.standardError == Double.MaxValue, "when testing standardError")
  }
  it should "return a container with expected stats after adding one value twice" in {
    val value = 3

    val container = LongQuantityStatsContainer.empty.add(value).add(value)

    assert(container.count == 2, "when testing count")

    val stats = container.toStats

    assert(stats.min == value, "when testing min")
    assert(stats.max == value, "when testing max")
    assert(stats.mean == value.toDouble, "when testing mean")
    assert(stats.standardError == 0.0, "when testing standardError")
  }
  it should "return a container with expected stats after adding two values" in {
    val value1 = -2
    val value2 = 2
    val expectedMean = 0.0
    val expectedStandardError = 2.0

    val container = LongQuantityStatsContainer.empty.add(value1).add(value2)

    assert(container.count == 2, "when testing count")
    assert(container.sum == 0, "when testing sum")

    val stats = container.toStats

    assert(stats.min == value1, "when testing min")
    assert(stats.max == value2, "when testing max")
    assert(stats.mean == expectedMean, "when testing mean")
    assert(stats.standardError == expectedStandardError, "when testing standardError")
  }
  "LongQuantityStatsContainer.apply" should "return a container with expected stats when passing in a large value" in {
    val value = Long.MinValue
    val expectedMean = value
    val expectedStandardError = 0.0

    val container = LongQuantityStatsContainer(value)

    assert(container.count == 1, "when testing count")
    assert(container.sum == value, "when testing sum")

    val stats = container.toStats

    assert(stats.min == value, "when testing min")
    assert(stats.max == value, "when testing max")
    assert(stats.mean == expectedMean, "when testing mean")
    assert(stats.standardError == Double.MaxValue, "when testing standardError")
  }
  it should "return a container with expected stats when passing in a list with the same large value several times" in {
    val value = Long.MaxValue
    val count = 100
    val values = (1 to count).map { _ => value }
    val expectedMean = value
    val expectedStandardError = 0.0

    val container = LongQuantityStatsContainer(values)

    assert(container.count == count, "when testing count")
    assert(container.sum == BigInt(value) * count, "when testing sum")

    val stats = container.toStats

    assert(stats.min == value, "when testing min")
    assert(stats.max == value, "when testing max")
    assert(stats.mean == expectedMean, "when testing mean")
    assert(stats.standardError == 0.0, "when testing standardError")
  }
  it should "return a container with expected stats when passing in a list of different values" in {
    val count = 5
    val values = (1 to count).map { _.toLong }
    val expectedMean = 3.0
    val expectedStandardErrorLowerBound = 0.70710
    val expectedStandardErrorUpperBound = 0.70711

    val container = LongQuantityStatsContainer(values)

    assert(container.count == count, "when testing count")
    assert(container.sum == values.sum, "when testing sum")

    val stats = container.toStats

    assert(stats.min == values.min, "when testing min")
    assert(stats.max == values.max, "when testing max")
    assert(stats.mean == expectedMean, "when testing mean")
    assert(stats.standardError > expectedStandardErrorLowerBound, "when testing standardError lower bound")
    assert(stats.standardError < expectedStandardErrorUpperBound, "when testing standardError upper bound")
  }
}
