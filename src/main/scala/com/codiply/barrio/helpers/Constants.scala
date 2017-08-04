package com.codiply.barrio.helpers

object Constants {
  val statsTimeoutMilliseconds = 60000

  private val slightlyReduceTimeoutFactor = 0.98
  private val slightlyIncreaseTimeoutFactor = 1.0 / slightlyReduceTimeoutFactor

  def slightlyReduceTimeout(timeout: Int): Int = (slightlyReduceTimeoutFactor * timeout).round.toInt
  def slightlyIncreaseTimeout(timeout: Int): Int = (slightlyIncreaseTimeoutFactor * timeout).round.toInt
}
