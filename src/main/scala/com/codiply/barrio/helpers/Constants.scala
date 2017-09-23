package com.codiply.barrio.helpers

object Constants {
  val statsTimeoutMilliseconds = 60000

  private val slightlyReduceTimeoutFactor = 0.98
  private val slightlyIncreaseTimeoutFactor = 1.0 / slightlyReduceTimeoutFactor
  private val considerablyIncreaseTimeoutFactor = 1.5

  def slightlyReduceTimeout(timeout: Long): Long = (slightlyReduceTimeoutFactor * timeout).round
  def slightlyIncreaseTimeout(timeout: Long): Long = (slightlyIncreaseTimeoutFactor * timeout).round
  def considerablyIncreaseTimeout(timeout: Long): Long = (considerablyIncreaseTimeoutFactor * timeout).round
}

object NodeRoles {
  val seedOnlyNode = "SeedOnlyNode"
  val fullNode = "FullNode"
}
