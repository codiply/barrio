package com.codiply.barrio.helpers

import scala.math.sqrt

object LongQuantityStatsContainer {
  val empty: LongQuantityStatsContainer =
    LongQuantityStatsContainer(
      min = Long.MaxValue,
      max = Long.MinValue,
      count = 0,
      sum = 0,
      sumOfSquares = 0)

  def apply(value: Long): LongQuantityStatsContainer =
    empty.add(value)
  def apply(values: Iterable[Long]): LongQuantityStatsContainer =
    empty.add(values)
}

final case class LongQuantityStatsContainer(
    min: Long,
    max: Long,
    count: BigInt,
    sum: BigInt,
    sumOfSquares: BigInt) {
  def add(value: Long): LongQuantityStatsContainer = {
    val valueBigInt = BigInt(value)
    LongQuantityStatsContainer(
        min = Math.min(min, value),
        max = Math.max(max, value),
        count = count + 1,
        sum = sum + valueBigInt,
        sumOfSquares = sumOfSquares + valueBigInt * valueBigInt)
  }

  def add(values: Iterable[Long]): LongQuantityStatsContainer =
    values.foldLeft(this)((container: LongQuantityStatsContainer, v: Long) => container.add(v))

  private def getStandardError: Double = {
    if (count < 2) {
      Double.MaxValue
    } else {
      val numerator = count * sumOfSquares - sum * sum
      val denominator = count * count * (count - 1)
      sqrt(numerator.toDouble / denominator.toDouble)
    }
  }

  def toStats(): LongQuantityStats = {
    LongQuantityStats(
        min = min,
        max = max,
        mean = if (count > 0) { sum.toDouble / count.toDouble } else { 0.0 },
        standardError = getStandardError)
  }
}

final case class LongQuantityStats(
    min: Long,
    max: Long,
    mean: Double,
    standardError: Double)
