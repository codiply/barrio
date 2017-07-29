package com.codiply.barrio.geometry

final case class EasyDistance(value: Double) {
  def lessThan(that: EasyDistance): Boolean = this.value < that.value
  def lessEqualThan(that: EasyDistance): Boolean = this.value <= that.value
}

final case class RealDistance(value: Double) {
  def lessThan(that: RealDistance): Boolean = this.value < that.value
  def lessEqualThan(that: RealDistance): Boolean = this.value <= that.value
}
