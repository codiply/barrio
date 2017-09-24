package com.codiply.barrio.helpers

object Constants {
  val statsTimeoutMilliseconds = 60000

  private val slightlyReduceTimeoutFactor = 0.98
  private val slightlyIncreaseTimeoutFactor = 1.0 / slightlyReduceTimeoutFactor
  private val considerablyReduceTimeoutFactor = 0.5
  private val considerablyIncreaseTimeoutFactor = 1.5

  def halveTimeout(timeout: Long): Long = (0.5 * timeout).round
  def slightlyReduceTimeout(timeout: Long): Long = (slightlyReduceTimeoutFactor * timeout).round
  def slightlyIncreaseTimeout(timeout: Long): Long = (slightlyIncreaseTimeoutFactor * timeout).round
  def considerablyReduceTimeout(timeout: Long): Long = (considerablyReduceTimeoutFactor * timeout).round
  def considerablyIncreaseTimeout(timeout: Long, times: Int = 1): Long = (times * considerablyIncreaseTimeoutFactor * timeout).round
}

object NodeRoles {
  val seedOnlyNode = "SeedOnlyNode"
  val fullNode = "FullNode"
}
