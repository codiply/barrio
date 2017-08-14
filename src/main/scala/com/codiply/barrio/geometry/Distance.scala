package com.codiply.barrio.geometry

object EasyDistance {
  val zero = EasyDistance(0.0)

  def min(distance1: EasyDistance, distance2: EasyDistance): EasyDistance =
    if (distance1.lessEqualThan(distance2)) {
      distance1
    } else {
      distance2
    }
}

final case class EasyDistance(value: Double) {
  def lessThan(that: EasyDistance): Boolean = this.value < that.value
  def lessEqualThan(that: EasyDistance): Boolean = this.value <= that.value
}

object RealDistance {
  val zero = RealDistance(0.0)

  def min(distance1: RealDistance, distance2: RealDistance): RealDistance =
    if (distance1.lessEqualThan(distance2)) {
      distance1
    } else {
      distance2
    }
}

final case class RealDistance(value: Double) {
  def lessThan(that: RealDistance): Boolean = this.value < that.value
  def lessEqualThan(that: RealDistance): Boolean = this.value <= that.value
}
