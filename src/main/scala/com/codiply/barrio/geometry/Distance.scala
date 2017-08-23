package com.codiply.barrio.geometry

object EasyDistance {
  def apply(value: Double): EasyDistance = new EasyDistance(value)

  val zero = EasyDistance(0.0)

  def min(distance1: EasyDistance, distance2: EasyDistance): EasyDistance =
    if (distance1 <= distance2) {
      distance1
    } else {
      distance2
    }
}

final class EasyDistance(val value: Double) extends AnyVal {
  def <(that: EasyDistance): Boolean = this.value < that.value
  def <=(that: EasyDistance): Boolean = this.value <= that.value
  def >(that: EasyDistance): Boolean = this.value > that.value
  def >=(that: EasyDistance): Boolean = this.value >= that.value
}

object RealDistance {
  def apply(value: Double): RealDistance = new RealDistance(value)

  val zero = new RealDistance(0.0)

  def min(distance1: RealDistance, distance2: RealDistance): RealDistance =
    if (distance1 <= distance2) {
      distance1
    } else {
      distance2
    }
}

final class RealDistance(val value: Double) extends AnyVal {
  def <(that: RealDistance): Boolean = this.value < that.value
  def <=(that: RealDistance): Boolean = this.value <= that.value
  def >(that: RealDistance): Boolean = this.value > that.value
  def >=(that: RealDistance): Boolean = this.value >= that.value
}
